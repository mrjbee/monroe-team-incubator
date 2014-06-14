function Model(){
	
	this.typeName = "Model"
	this.serverUrl = "http://194.29.62.160:8880/remote-config-1.0-SNAPSHOT/rest"
	this.presenter = null	
	this.username = "" 
	this.password = ""
	this.awakeMinutes = 0

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
		this.fetchAwakeMinutes(function(){
			me.presenter.doOnDetailsUpdated()
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