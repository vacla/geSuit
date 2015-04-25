SELECT t.`name`, t.`nickname`, t.`uuid`, t.`ip`, t.`firstseen`, t.`lastseen`, IF(b1.`date` IS NOT NULL, 'true', 'false') as `name_ban`, IF(b2.`date` IS NOT NULL, 'true', 'false') as `ip_ban`
FROM `%tracking%` AS t
LEFT JOIN `%bans%` AS b1
ON (
  t.`uuid`=b1.`who`
) 
AND b1.`action`='ban'
AND b1.`unban_id` IS NULL
AND (
  b1.`until` IS NULL
  OR b1.`until` > NOW()
)
LEFT JOIN `%bans%` AS b2 
ON (
  t.`ip`=b2.`who` 
) 
AND b2.`action`='ban'
AND b2.`unban_id` IS NULL
AND (
  b2.`until` IS NULL
  OR b2.`until` > NOW()
)
WHERE t.`ip`=?
GROUP BY t.`name`,t.`nickname`,t.`uuid`,t.`ip` 
ORDER BY t.`lastseen`;