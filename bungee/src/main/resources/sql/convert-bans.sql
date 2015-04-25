/*
This script will convert, using the tracking table, the bans table into the new banhistory table format.
This script assumes that the banhistory table already exists and has the correct schema, and that the tracking table has already been updated

TODO:
 - Need to make markers for all table names so they are configurable
*/

CREATE TEMPORARY TABLE `bans_wip` (`who` VARCHAR(32) NOT NULL, `type` ENUM('uuid', 'ip') NOT NULL, `reason` VARCHAR(255), `by_name` VARCHAR(20) NOT NULL, `by_uuid` CHAR(32), `action` ENUM('ban', 'unban') NOT NULL, `date` DATETIME NOT NULL, `until` DATETIME, `unban_id` INTEGER, `old_id` INTEGER, `active` SMALLINT(1), INDEX (`old_id`, `action`));

/* Fill the table with all ban records (no unbans) */
INSERT INTO `bans_wip` (
SELECT * 
FROM (
	(
		SELECT u.`banned_uuid` AS `who`, 'uuid' AS `type` , u.`reason`, u.`banned_by` AS `by_name`, NULL AS `by_uuid`,  'ban' AS `action`, u.`banned_on` AS `date`, u.`banned_until` AS `until`, NULL AS `unban_id`, u.`id` as `old_id`, u.`active`
		FROM (
			(
				SELECT `banned_playername`, `banned_uuid`, `banned_ip`, `banned_by`, `reason`, `type`, `active`, `banned_on`, `banned_until`, `id`
				FROM  `bans` 
				WHERE  `banned_uuid` IS NOT NULL 
				AND  `type` != 'warn'
			)
			UNION (
				SELECT b.`banned_playername`, t.`uuid`, NULL , b.`banned_by`, b.`reason`, b.`type`, b.`active`, b.`banned_on`, b.`banned_until`, `id`
				FROM  `bans` AS b
				JOIN  `tracking` AS t ON ( b.banned_playername = t.name ) 
				WHERE  `banned_uuid` IS NULL 
				AND  `banned_playername` IS NOT NULL 
				AND  `type` !=  'warn'
				GROUP BY t.uuid
			)
		) AS u
	)
	UNION (
		SELECT `banned_ip` AS `who`, 'ip' AS `type`, `reason`, `banned_by` AS `by_name`, NULL AS `by_uuid`, 'ban' AS `action`, `banned_on` AS `date`, `banned_until` AS `until`, NULL AS `unban_id`, `id` as `old_id`, `active`
		FROM  `bans`
		WHERE  `banned_ip` IS NOT NULL 
		AND  `type` =  'ipban'
	)
) AS z
ORDER BY z.date ASC
);

/* Inserts the unban records onto the end */
INSERT INTO `bans_wip` (
	SELECT `who`, `type`, 'Imported unban, date and by whom not known' AS `reason`, 'Unknown' AS `by_name`, NULL AS `by_uuid`, 'unban' AS `action`, `date`, NULL AS `until`, NULL AS `unban_id`, `old_id`, 2 AS `active`
	FROM `bans_wip`
	WHERE `action` = 'ban'
	AND `active` = 0
	AND `until` IS NULL
);

/* Resolve `by_name` to UUID if possible using the tracking table */
UPDATE `bans_wip` b
JOIN `tracking` t
ON b.`by_name` = t.`name`
SET b.`by_uuid` = t.`uuid`;

/* Reorder the table to the unbans are with the bans */
ALTER TABLE `bans_wip` 
ORDER BY `date` ASC, `active` ASC;

/* Add the real id column */
ALTER TABLE `bans_wip` 
ADD `id` INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY FIRST;

/* Link the bans and unbans */
UPDATE `bans_wip` a
LEFT JOIN `bans_wip` b
ON a.`old_id` = b.`old_id`
AND a.`action` = 'ban'
AND b.`action` = 'unban'
SET	a.`unban_id` = b.`id`
WHERE a.`action` = 'ban';

/* Move it all over to the real bans table */
DELETE FROM `banhistory`;

INSERT INTO `banhistory`
SELECT `id`, `who`, `type`, `reason`, `by_name`, `by_uuid`, `action`, `date`, `until`, `unban_id`
FROM `bans_wip`;

/* And some cleanup */
DROP TABLE `bans_wip`;
