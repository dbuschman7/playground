#!/bin/sh
set -x

. ./host.env

curl 	--request PUT \
	--header "Content-Type: multipart/form-data" \
	--header "Accept: application/json"\
	-w "\\nHTTP Response : %{http_code}\\n" \
	-F "data=@/Users/dbusch/Documents/Gasplant/ryan-synthetic-error.xml" \
	${HOST}/mongofs/upload	

