# quick start

1. compile
   ```shell
   ./gradlew clean install
   ```

2. config api key in plugins/TestPlugin folder
   ```shell
   mkdir -p plugins/TestPlugin
   touch custom-config.yml
   # redefine any custom config under resources folder such as apiKey
   ```

3. start in local
   ``` shell
   sh ./start.sh
   ```
   or start in docker
   ``` shell
   docker-compose build
   docker-compose up
   ```

4. visit the url in logs & scan the QR code to log in wechat
5. visit the api with correct api-key in headers
