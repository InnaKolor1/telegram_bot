;CREATE DATABASE telegram_bot;


 CREATE USER telegram_bot WITH PASSWORD '4683';




GRANT ALL PRIVILEGES ON DATABASE telegram_bot TO telegram_bot;


\c telegram_bot;

GRANT ALL ON SCHEMA public TO telegram_bot;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO telegram_bot;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO telegram_bot;


ALTER USER telegram_bot CREATEDB;