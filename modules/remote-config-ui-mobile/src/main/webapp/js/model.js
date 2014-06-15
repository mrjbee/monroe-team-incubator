function Model(){
	
	this.typeName = "Model"
	this.serverUrl = "http://192.168.0.201:8080/remote-config-1.0-SNAPSHOT/rest"
	this.presenter = null	
	this.username = "" 
	this.password = ""
	this.awakeMinutes = 0
	this.lastStatus = "NaN"
	this.lastDate = "NaN"
	this.offlineTillDate = "NaN"

	me = this
	$.ajaxSetup({
		beforeSend: function (request){
		        request.setRequestHeader("Avoid-WWW-Authenticate", "yes");
                request.setRequestHeader("Authorization", "Basic " + btoa(me.username + ":" + me.password));
            }
	});

	this.setPresenter = function (presenter){
		this.presenter = presenter
	}

	this.loginUser = function(loginRequestModel){
		me = this	
		this.username = loginRequestModel.userName 
		this.password = loginRequestModel.password
		doRequest({
	        type: "GET",
	        url: this.serverUrl+'/secure-ping',
		},function(response){
			if (response.statusCode == 401){
				me.presenter.doOnUserLogOut()
			}else if (response.statusCode == 200){
				me.presenter.doOnUserLogIn()
			}else {
				me.presenter.doOnError(response.statusCode);
			}
        })
	}
	
	this.updateDetails = function(){
		me.fetchAwakeMinutes(function(){
			me.fetchStatus(function(){
				me.fetchLastDate(function(){
					me.fetchOfflineTillDate(function(){
						me.presenter.doOnDetailsUpdated()
					})
				})
			})
		})
	}

	this.fetchAwakeMinutes = function(next){
		doRequest({
	        type: "GET",
	        url: this.serverUrl+'/server/moon/sleepminutes',
		},function(response){
			if (response.statusCode == 200){
				me.awakeMinutes = parseInt(response.resultText)
				next()
			}else {
				me.presenter.doOnError(response.statusCode);
			}
        })
	}

	this.fetchStatus = function(next){
		doRequest({
	        type: "GET",
	        url: this.serverUrl+'/server/moon/status',
		},function(response){
			if (response.statusCode == 200){
				me.lastStatus = response.resultText
				next()
			}else {
				me.presenter.doOnError(response.statusCode);
			}
        })
	}

	this.fetchLastDate = function(next){
		doRequest({
	        type: "GET",
	        url: this.serverUrl+'/server/moon/lastDate',
		},function(response){
			if (response.statusCode == 200){
				me.lastDate = response.resultText
				next()
			}else {
				me.presenter.doOnError(response.statusCode);
			}
        })
	}
	
	this.fetchOfflineTillDate = function(next){
		doRequest({
	        type: "GET",
	        url: this.serverUrl+'/server/moon/offlineTillDate',
		},function(response){
			if (response.statusCode == 200){
				me.offlineTillDate = response.resultText
				next()
			}else {
				me.presenter.doOnError(response.statusCode);
			}
        })
	}

	this.saveAwakeSeconds = function(value){
		doRequest({
	        type: "POST",
	        url: this.serverUrl+'/server/moon/sleepminutes',
	        data: ""+value
		},function(response){
			if (response.statusCode == 200){
				me.awakeMinutes = parseInt(response.resultText)
				me.presenter.doOnAwakeFetch(me.awakeMinutes)
			}else {
				me.presenter.doOnError(response.statusCode);
			}
        })
	}

}

function doRequest(ajaxDetails, callback){
	$.ajax(ajaxDetails).always(function(dataorJQXHR, textStatus, jqXHRorErrorThrown) {
			if (textStatus == "success"){
     			callback({
     				statusCode:jqXHRorErrorThrown.status, 
     				resultText:dataorJQXHR
     			})
     		} else {
     			callback({
     				statusCode:dataorJQXHR.status 
     			})
     		}
 	 });
}