#!/bin/bash
echo "changing /tmp permission..." >> /tmp/log/bash-scripts-logs/start-logs.log
chmod +x /tmp/
echo "connecting to rabbitmq container..." >> /tmp/log/bash-scripts-logs/start-logs.log
while ! nc -z rabbitmq-container 5672; do sleep 3; done
echo "Done" >> /tmp/log/bash-scripts-logs/start-logs.log
echo "connecting to redis container..." >> /tmp/log/bash-scripts-logs/start-logs.log
while ! nc -z redis-container 6379; do sleep 3; done
echo "Done" >> /tmp/log/bash-scripts-logs/start-logs.log
bash /rabbitmq-init.sh