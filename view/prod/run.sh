#!/bin/bash
for mainFileName in `ls /usr/share/nginx/html/main*.js`;
do
	envsubst '$BACKEND_API_URL' < ${mainFileName} > main.tmp
	mv main.tmp ${mainFileName}
done
nginx -g 'daemon off;'
