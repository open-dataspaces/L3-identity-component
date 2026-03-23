set PG_DUMP_PATH="C:\Program Files\PostgreSQL\17\bin\pg_dump.exe"
set OUTPUT_PATH="c:\dev\"
set PGPASSWORD=password

set DATETIME=%DATE:~0,4%%DATE:~5,2%%DATE:~8,2%_%TIME:~0,2%%TIME:~3,2%%TIME:~6,2%
set DATETIME=%DATETIME: =0%

%PG_DUMP_PATH% -h ::1 -w -p 5433 -U keycloak --schema-only -n auth db_ods >%OUTPUT_PATH%db_dump_%DATETIME%.sql
