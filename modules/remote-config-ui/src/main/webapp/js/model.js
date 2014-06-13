function Model(){
	
	this.typeName = "Model"
	this.serverUrl = "http://localhost:8080/remote-config-1.0-SNAPSHOT/rest"
	this.presenter = null	
	this.username = "" 
	this.password = ""
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
			}
			if (response.statusCode == 200){

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
     				statusCode:jqXHRorErrorThrown.status 
     			})
     		} else {
     			callback({
     				statusCode:dataorJQXHR.status 
     			})
     		}
 	 });
}