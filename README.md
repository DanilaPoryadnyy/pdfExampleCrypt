# Пример реализации электронной подписи PDF документа с помощью Java и CryptoPro

## Требования к окружению
- JDK 8
- Установленный JCP CryptoPro в jre/lib

## Установка сертификата
1. Сгенерируйте и установите сертификат с сайта [CryptoPro](https://www.cryptopro.ru/certsrv/)

## Переменные
- STORE_PASS = "пароль_хранилища".toCharArray();
- keystorePath = "путь_к_хранилищу";
- alias = "имя сертификата";
- filePath = "путь_к_файлу";
- fileOut = "путь_к_созданному_файлу";

### Пример

Проверка файла на тестовую ЭП на сайте проверка-подписи.рф

![image](https://github.com/DanilaPoryadnyy/pdfExampleCrypt/assets/114912900/8d13c6c0-5b44-4f48-aae6-0076bf2b2f4a)


Подписанный PDF файл:

[output.pdf](https://github.com/DanilaPoryadnyy/pdfExampleCrypt/files/14076679/output.pdf)
