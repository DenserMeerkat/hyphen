importScripts('sqlite3.js');

let sqlite3 = null;

const databases = new Map();
const statements = new Map();

let nextDatabaseId = 0;
let nextStatementId = 0;

function openRequest(id, requestData) {
    try {
        const newDatabaseId = nextDatabaseId++;
        let newDatabase;

        if (sqlite3.capi.sqlite3_vfs_find('opfs')) {
            newDatabase = new sqlite3.oo1.OpfsDb(requestData.fileName || '/hyphen.db');
        } else {
            newDatabase = new sqlite3.oo1.DB(requestData.fileName || '/hyphen.db', 'ct');
        }

        databases.set(newDatabaseId, newDatabase);
        postMessage({'id': id, data: {'databaseId': newDatabaseId}});
    } catch (error) {
        postMessage({'id': id, error: error.message});
    }
}

function prepareRequest(id, requestData) {
    try {
        const newStatementId = nextStatementId++;
        const resultData = {
            'statementId': newStatementId,
            'parameterCount': 0,
            'columnNames': []
        };
        const database = databases.get(requestData.databaseId);

        if (!database) {
            postMessage({'id': id, error: "Invalid database ID: " + requestData.databaseId});
            return;
        }

        const statement = database.prepare(requestData.sql);
        statements.set(newStatementId, statement);

        resultData.parameterCount = sqlite3.capi.sqlite3_bind_parameter_count(statement);
        for (let i = 0; i < statement.columnCount; i++) {
            resultData.columnNames.push(sqlite3.capi.sqlite3_column_name(statement, i));
        }

        postMessage({'id': id, data: resultData});
    } catch (error) {
        postMessage({'id': id, error: error.message});
    }
}

function stepRequest(id, requestData) {
    const statement = statements.get(requestData.statementId);
    if (!statement) {
        postMessage({'id': id, error: "Invalid statement ID: " + requestData.statementId});
        return;
    }

    try {
        const resultData = {
            'rows': [],
            'columnTypes': []
        };

        statement.reset();
        statement.clearBindings();

        for (let i = 0; i < requestData.bindings.length; i++) {
            statement.bind(i + 1, requestData.bindings[i]);
        }

        while (statement.step()) {
            if (!resultData.columnTypes.length) {
                for (let i = 0; i < statement.columnCount; i++) {
                    resultData.columnTypes.push(sqlite3.capi.sqlite3_column_type(statement, i));
                }
            }
            resultData.rows.push(statement.get([]));
        }

        postMessage({'id': id, data: resultData});
    } catch (error) {
        postMessage({'id': id, error: error.message});
    }
}

function closeRequest(id, requestData) {
    if (requestData.statementId !== undefined && requestData.statementId != null) {
        const statement = statements.get(requestData.statementId);
        if (statement) {
            try {
                statement.finalize();
                statements.delete(requestData.statementId);
            } catch (error) {
            }
        }
    }

    if (requestData.databaseId !== undefined && requestData.databaseId != null) {
        const database = databases.get(requestData.databaseId);
        if (database) {
            try {
                database.close();
                databases.delete(requestData.databaseId);
            } catch (error) {
            }
        }
    }
}

const commandMap = {
    'open': openRequest,
    'prepare': prepareRequest,
    'step': stepRequest,
    'close': closeRequest,
};

function handleMessage(e) {
    const requestMsg = e.data;

    if (!requestMsg.data || !requestMsg.data.cmd) {
        postMessage({'id': requestMsg.id, 'error': "Invalid request format."});
        return;
    }

    const command = requestMsg.data.cmd;
    const requestHandler = commandMap[command];

    if (requestHandler) {
        requestHandler(requestMsg.id, requestMsg.data);
    } else {
        postMessage({'id': requestMsg.id, 'error': "Unknown command: '" + command + "'."});
    }
}

const messageQueue = [];
onmessage = (e) => {
    if (!sqlite3) {
        messageQueue.push(e);
    } else {
        handleMessage(e);
    }
};

self.sqlite3InitModule({
    print: function() {},
    printErr: function() {},
}).then(instance => {
    sqlite3 = instance;
    while (messageQueue.length > 0) {
        handleMessage(messageQueue.shift());
    }
}).catch(e => {
    postMessage({'id': -1, 'error': "Failed to initialize sqlite3: " + e.message});
});