### Simple HTTP Client (to one page for test)

В данном разделе мы не изучаем Angular, None.js и т.д. Для тестирования средств безопасности нашего Spring приложения
нам нужен простой HTTP клиент, вот его мы соберем на Angular-е (говорят можно проще - сделать HTML страничку с JS скриптом),
но мы легких путей не ищем.

Тут мы просто опишем, что делали по шагам:
- #### Шаг 1. - Идем на [официальный сайт Node.js](https://nodejs.org/en/download/), качаем его и устанавливаем.
- #### Шаг 2. - Проверяем все ли установилось, в терминале, смотрим версии самого фреймворка:

       node -v

а так же NPM:

       npm -v

NPM (Node Package Manager) — это менеджер пакетов для модулей Node.js. Он помогает разработчикам управлять зависимостями 
проекта, скриптами и сторонними библиотеками. При установке Node.js в нашей системе NPM устанавливается автоматически и 
готов к использованию.

В основном он используется для управления пакетами или модулями — это готовые фрагменты кода, расширяющие функциональность 
нашего Node.js приложения. Реестр NPM содержит миллионы бесплатных пакетов, которые мы можем загрузить и использовать в 
своем проекте. NPM устанавливается автоматически при установке Node.js, поэтому нам не нужно настраивать его вручную.

- #### Шаг 3. - Создаем папку для будущего HTTP клиента: 

      mkdir js_client

- #### Шаг 4. - Переходим в папку с будущим проектом и запускаем команду создания нового проекта, а так же отвечаем на вопросы:

      cd js_client

      ng new
        
      ? What name would you like to use for the new workspace and initial project?  - даем имя например: client
      ? Which stylesheet format would you like to use? - выбираем вариант: CSS
      ? Do you want to enable Server-Side Rendering (SSR) and Static Site Generation (SSG/Prerendering)? - выбираем вариант: No

Важное различие здесь — это разница между Server Side Rendering , Static Site Generation и Single Page Application. 

Краткое значение этих терминов:

- Single Page Application : все приложение загружается на клиенте, и загружается одна страница. Вид навигации по 
приложению динамически контролируется через JavaScript на клиенте.
- Server Side Rendering : вместо того, чтобы полностью доставлять приложение клиенту, приложение будет работать на 
внешнем сервере. Запросы на страницы могут быть сделаны на этом сервере, сервер будет рендерить эту страницу, а 
затем доставлять ее клиенту.
- Static Site Generation : приложение создается один раз для создания множества различных статических страниц, а 
затем эти страницы загружаются непосредственно с сервера (а не генерируются сервером на лету).

- #### Шаг 5. - Устанавливаем консольный интерфейс для нашего проекта (все операции, ранние и текущие производим в папке проекта):

      npm install @angular/cli

После всех предыдущих манипуляций у нас появится проект с примерной структурой:

![MountFolders](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_7_1/DOC/JPG/CORS/JS_client_structure.jpg)

Далее мы доводим, как можем, до ума наш HTTP клиент. Еще раз, цель не изучить Angular и JS, а сделать, буквально одну 
страницу, которая будет обращаться к нашему Spring приложению и дергать вполне определенный эндпоинт. И сначала мы 
должны в браузере получить ошибку "CORS", а затем, сделав определенные настройки в системе безопасности Spring-a добиться,
чтобы данные с нашего эндпоинта залетали в наш JS HTTP клиент и отображались в браузере - т.е. связь есть и клиент 
успешно 'дергает' сервер.

- #### Шаг 6. - Запускаем HTTP клиент:

      ng serve

- #### Шаг 7. - Переходим в браузер и обращаемся по адресу:

      http://localhost:4200/

Если Spring приложение запущено, и настройки CORS в Spring Security filterChain сделаны должным образом, то JS HTTP клиент 
обратится к нему, и отобразит данные полученные из контроллера [NoticesController.java](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_7_1/src/main/java/me/oldboy/controllers/api/NoticesController.java) - это строка: "Here are the notices details from the DB"

См. [описание в ReadMe файле Spring приложения](https://github.com/JcoderPaul/SPRING_SECURITY-Short_Guide/blob/master/Security_part_7_1/ReadMe.md).

Фича приложения сделанного при помощи Angular в том, что после его запуска, изменения сделанные в среде разработки 
(Visual Studio и т.п.) в любом компоненте приложения автоматически прогружаются (на лету, без специальной остановки) 
и идут в работу тут же - мы видим их немедленно.

При желании мы можем установить в дополнении к IntelliJ IDEA, например, Visual Studio для работы с SJ (это будет предложено). 
________________________________________________________________________________________________________________________

Статьи и материалы помогавшие в экспресс изучении вопроса:
- [Build a Simple Web App with Express & Angular](https://www.geeksforgeeks.org/build-a-simple-web-app-with-express-angular/).
- [NodeJS NPM](https://www.geeksforgeeks.org/node-js-npm-node-package-manager/).
- [Учебное пособие по Node.js](https://www.geeksforgeeks.org/nodejs/).
- [Creating your first Angular application](https://angularstart.com/modules/angular-getting-started/1/).
- [How To Use HttpClient in Angular?](https://www.geeksforgeeks.org/how-to-use-httpclient-in-angular/)
- [httpClient-demo (GitHub repository)](https://github.com/jtklier/httpClient-demo).
________________________________________________________________________________________________________________________

#### Тут будет размещена только папка SRC нашего HTTP клиента, т.к. основной массив кода генерируется Node.js (Angular), сопутствующими командами настройки и не требует столь трепетного к нему отношения. Тем более именно в SRC мы проделали все основные манипуляции с кодом нашего HTTP клиента.
