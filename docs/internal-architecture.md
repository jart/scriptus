
#Scriptus: application architecture

The scriptus codebase can be divided into several sections.

The goal of the architecture is to run script processes, persisting them during API calls, and allowing them to interact with users via an interaction medium.

The DAO (data access object) is responsible for any short- or long-term persistence of Script processes.

The interaction medium object is used by the script process (via the API) to interact with users.

The interaction medium and DAO have three separate implementations each, and which one is used at runtime is configured in ScriptusConfig and the property file backing it. Scriptus is reloaded when the settings are changed via the admin interface.

The process scheduler is responsible for executing script processes, and administering the task scheduler used for waking sleeping processes and managing timeouts.

The ScriptProcess object contains the logic used to execute the scripts, persist the process as a continuation, and interact with the API.

The API itself is implemented in the ScriptusAPI object, which serves as the "global" object of the JavaScript environment, in much the same way that "window" does in a browser.

The API calls are implemented using a visitor pattern. Each call has an object e.g. the class Fork, which overrides visit(...) to execute the specific call logic.


