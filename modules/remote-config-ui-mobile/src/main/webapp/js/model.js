var ModelPrototype = {

    _presenter:null,

    _serverUrl : "http://192.168.0.201:8080/remote-config-1.0-SNAPSHOT/rest",

    _username : "",
    _password : "",

    //Server statistic fields
    _awakeMinutes : 0,
    _lastStatus : "NaN",
    _lastDate : "NaN",
    _offlineTillDate : "NaN",

    constructor:function _constructor(presenter){
        this._presenter = presenter;
        $.ajaxSetup({
            beforeSend: function (request) {
                request.setRequestHeader("Avoid-WWW-Authenticate", "yes");
                request.setRequestHeader("Authorization", "Basic " + btoa(this._username + ":" + this._password));
            }.bind(this)
        });
    },

    loginUser : function _loginUser (loginRequestModel) {
        this._username = loginRequestModel.userName;
        this._password = loginRequestModel._password;
        this._doRequest({
            type: "GET",
            url: this._serverUrl + '/secure-ping'
        }, function (response) {
            if (response.statusCode == 401) {
                this._presenter.doOnUserLogOut();
            } else if (response.statusCode == 200) {
                this._presenter.doOnUserLogIn();
            } else {
                this._presenter.doOnError(response.statusCode);
            }
        }.bind(this))
    },

    updateDetails : function _updateDetails () {
        this.fetchAwakeMinutes(function () {
            this.fetchStatus(function () {
                this.fetchLastDate(function () {
                    this.fetchOfflineTillDate(function () {
                        this._presenter.doOnDetailsUpdated();
                    }.bind(this))
                }.bind(this))
            }.bind(this))
        }.bind(this))
    },

    fetchAwakeMinutes: function _fetchAwakeMinutes(next) {
        this._doRequest({
            type: "GET",
            url: this._serverUrl + '/server/moon/sleepminutes'
        }, function (response) {
            if (response.statusCode == 200) {
                this._awakeMinutes = parseInt(response.resultText);
                next()
            } else {
                this._presenter.doOnError(response.statusCode);
            }
        }.bind(this))
    },

    fetchStatus : function (next) {
        this._doRequest({
            type: "GET",
            url: this._serverUrl + '/server/moon/status'
        }, function (response) {
            if (response.statusCode == 200) {
                this._lastStatus = response.resultText;
                next();
            } else {
                this._presenter.doOnError(response.statusCode);
            }
        }.bind(this))
    },

    fetchLastDate : function (next) {
        this._doRequest({
            type: "GET",
            url: this._serverUrl + '/server/moon/lastDate'
        }, function (response) {
            if (response.statusCode == 200) {
                this._lastDate = response.resultText;
                next()
            } else {
                this._presenter.doOnError(response.statusCode);
            }
        }.bind(this))
    },

    fetchOfflineTillDate : function (next) {
        this._doRequest({
            type: "GET",
            url: this._serverUrl + '/server/moon/offlineTillDate'
        }, function (response) {
            if (response.statusCode == 200) {
                this._offlineTillDate = response.resultText;
                next()
            } else {
                this._presenter.doOnError(response.statusCode);
            }
        }.bind(this))
    },

    saveAwakeSeconds : function (value) {
        this._doRequest({
            type: "POST",
            url: this._serverUrl + '/server/moon/sleepminutes',
            data: "" + value
        }, function (response) {
            if (response.statusCode == 200) {
                this._awakeMinutes = parseInt(response.resultText);
                this._presenter.doOnAwakeFetch(this._awakeMinutes)
            } else {
                this._presenter.doOnError(response.statusCode);
            }
        }.bind(this))
    },

    _doRequest: function __doRequest(ajaxDetails, callback) {
        $.ajax(ajaxDetails).always(function (dataorJQXHR, textStatus, jqXHRorErrorThrown) {
            if (textStatus == "success") {
                callback({
                    statusCode: jqXHRorErrorThrown.status,
                    resultText: dataorJQXHR
                })
            } else {
                callback({
                    statusCode: dataorJQXHR.status
                })
            }
        });
    }

};