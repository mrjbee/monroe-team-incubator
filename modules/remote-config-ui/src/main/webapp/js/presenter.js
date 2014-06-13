function Presenter(model, view){

	this.model = model;
	this.typeName = "Presenter"
	this.view = view;

	model.presenter = this;
	
	this.doOnStartup = function(){
		view.loginBtn.click(this.doOnLoggingBtnClick)
		view.authPanel.slideToggle("slow");
	}

	this.doOnLoggingBtnClick = function(){
		var userName = view.userNameInput.val();
		var password = view.passInput.val();
		view.authPanel.slideToggle();
		view.waitProgressBar.fadeToggle();
	}
	
}