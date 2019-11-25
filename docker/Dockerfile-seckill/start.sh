#!/bin/bash
while ! nc -z rabbitmq-container 5672; do sleep 3; done
while ! nc -z redis-container 6379; do sleep 3; done

bash /rabbitmq-init.sh