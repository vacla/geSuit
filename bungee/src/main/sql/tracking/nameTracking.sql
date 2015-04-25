SELECT t2.`name`, t2.`nickname`, t2.`uuid`, t2.`ip`, t2.`firstseen`, t2.`lastseen`, IF(b1.`date` IS NOT NULL, 1, 0) as `name_ban`, IF(b2.`date` IS NOT NULL, 1, 0) as `ip_ban`
FROM `%tracking%` AS t1
JOIN `%tracking%` AS t2 
ON (
  t1.`ip`=t2.`ip`
  OR t1.`uuid`=t2.`uuid`
) 
LEFT JOIN `%bans%` AS b1
ON (
  t2.`uuid`=b1.`who`
) 
AND b1.`action`='ban'
AND b1.`unban_id` IS NULL
AND (
  b1.`until` IS NULL
  OR b1.`until` > NOW()
)
LEFT JOIN `%bans%` AS b2 
ON (
  t2.`ip`=b2.`who` 
) 
AND b2.`action`='ban'
AND b2.`unban_id` IS NULL
AND (
  b2.`until` IS NULL
  OR b2.`until` > NOW()
)
WHERE t1.`name`=?
GROUP BY t2.`name`,t2.`nickname`,t2.`uuid`,t2.`ip` 
ORDER BY t2.`lastseen`;
