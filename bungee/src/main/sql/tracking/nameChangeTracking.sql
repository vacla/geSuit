SELECT p1.`name`, p1.`nickname`, p1.`uuid`, p1.`ip`, p1.`firstseen`, p1.`lastseen`, 0 as `name_ban`, 0 as `ip_ban`
FROM `%tracking%` p1
INNER JOIN (
    SELECT max(lastseen) lastseen, name 
    FROM `%tracking%`
    WHERE uuid=?
    GROUP BY name
) p2
ON p1.name = p2.name
AND p1.lastseen = p2.lastseen
WHERE p1.uuid=?
ORDER BY p1.lastseen DESC;