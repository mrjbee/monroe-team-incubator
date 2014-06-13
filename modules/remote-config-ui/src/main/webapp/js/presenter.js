function Presenter(model, view){

	this.typeName = "Presenter"
	
	this.model = model;
	this.view = view;

	this.doOnStartup = function(){
		model.setPresenter(this)
		view.loginBtn.click(this.doOnLoggingBtnClick)
		view.authPanel.slideDown("slow");
	}

	this.doOnLoggingBtnClick = function(){
		var userNameTxt = view.userNameInput.val();
		var passwordTxt = view.passInput.val();
		view.authPanel.slideUp();
		view.waitProgressBar.fadeIn();
		var loginRequestModel = {
			userName:userNameTxt,
			password:passwordTxt
		}
		model.loginUser(loginRequestModel)	
	}

	this.doOnUserLogOut = function() {
		view.authPanel.slideDown();
		view.waitProgressBar.fadeOut();
		view.infolabel.text("Authorization fails! Try again...")
		view.infoPanel.slideDown().delay(800).fadeOut(400);	
	}

	this.doOnError = function(statusCode){
		view.waitProgressBar.fadeOut();
		view.infolabel.text("Error ("+statusCode+") ! Please try again later...")
		view.infoPanel.slideDown().delay(800).fadeOut(400);	
		view.authPanel.slideDown("slow");
	}
	
}