-- Write MySQL query to find IPs that mode more than a certain number of requests for a given time period.

SELECT id, INET_NTOA(ip_address), date_time, COUNT(ip_address) FROM parser.request_log
WHERE date_time >= "2017-01-01.00:00:00" AND date_time <= "2017-01-02.00:00:00"
GROUP BY ip_address HAVING COUNT(ip_address) >= 500;

-- Write MySQL query to find requests made by a given IP.
SELECT id, INET_NTOA(ip_address), date_time FROM parser.request_log WHERE ip_address = INET_ATON("192.168.102.136")