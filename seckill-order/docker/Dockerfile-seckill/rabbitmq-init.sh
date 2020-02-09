#!/bin/bash

# get all vhost
# create new vhost
# return 201 meaning creation is done, 204 means vhost already there
#

# parse the return json, chekc 'status' field to see if it is 'OK'
aliveStatus=$(curl -u guest:guest -H "content-type:application/json" -XGET http://rabbitmq-container:15672/api/aliveness-test/seckill-mq-vhost)

result=$(echo ${aliveStatus} | jq -r '.status')

if [ ${result} = "ok" ]; then
    echo "seckill-mq-vhost aliveStatus ok" >> /tmp/log/bash-scripts-logs/rabbitmq-init-logs
else
    curl -i -u guest:guest -H "content-type:application/json" -XPUT http://rabbitmq-container:15672/api/vhosts/seckill-mq-vhost
    # make curl call to auth api to send an email to notify me vhost not alive.
    echo "seckill-mq-vhost aliveStatus not ok" >> /tmp/log/bash-scripts-logs/rabbitmq-init-logs
fi

# listing all user, check if user "ggininder" exists, if not create one
alluser=$(curl -u guest:guest -H "content-type:application/json" -XGET http://rabbitmq-container:15672/api/users)


# find if username "ggininder"(admin acc) exists
echo "found user in rabbitmq db: $alluser"  >> /tmp/log/bash-scripts-logs/rabbitmq-init-logs
flag=
for username in $(echo $alluser | jq -r '.[].name'); do
    if [ ${username} = "ggininder" ]; then
        flag="true"
    fi
done

if [ $flag ]; then
    echo "admin acc exits" >> /tmp/log/bash-scripts-logs/rabbitmq-init-logs

else
    echo "admin acc NOT exits" >> /tmp/log/bash-scripts-logs/rabbitmq-init-logs
    echo "creating admin acc..." >> /tmp/log/bash-scripts-logs/rabbitmq-init-logs
    curl -u guest:guest -H "content-type:application/json" -XPUT --data '{"password":"ggininder87", "tags": "admin"}' http://rabbitmq-container:15672/api/users/ggininder87
fi
curl -u guest:guest -H "content-type:application/json" -XPUT --data '{"configure":".*","write":".*","read":".*"}' http://rabbitmq-container:15672/api/permissions/seckill-mq-vhost/ggininder87
java -Dspring.profiles.active=dev -jar /var/seckill/seckill.jar --cache.redis.host=redis-container --app.mqhost=rabbitmq-container