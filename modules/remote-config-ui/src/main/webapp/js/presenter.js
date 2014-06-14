function Presenter(model, view){

	this.typeName = "Presenter"
	
	this.model = model;
	this.view = view;
	this.maxSleepMinutes = 360;
	thisPresentor = this


	this.updateAwakeSecondsUI = function(minutes){
		if(minutes == 0){
			view.awakeMinutesLabel.text("stay up")
		} else {
			hr = Math.floor(minutes/60);
			mins= minutes % 60 
			if (hr == 0){
				view.awakeMinutesLabel.text(mins+"min")
			} else {
				view.awakeMinutesLabel.text(hr+" hr "+mins+"min")
			}
		}
		view.awakeMinutesSlider.slider('value', Math.round(100 * minutes/this.maxSleepMinutes))
	}

	this.doOnStartup = function(){
		model.setPresenter(this)
		view.loginBtn.click(this.doOnLoggingBtnClick)
		view.authPanel.slideDown("slow");
		view.awakeMinutesSlider.slider({
			slide: function( event, ui ) {
				if (ui.value == 0){
					thisPresentor.updateAwakeSecondsUI(0);
				} else {
					thisPresentor.updateAwakeSecondsUI(Math.round(thisPresentor.maxSleepMinutes/100*ui.value));
				}
			},
			stop: function( event, ui ) {
				if (ui.value == 0){
					thisPresentor.model.saveAwakeSeconds(0);
				} else {
					thisPresentor.model.saveAwakeSeconds(Math.round(thisPresentor.maxSleepMinutes/100*ui.value));
				}				
			}
		});
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

	this.doOnUserLogIn = function() {
		model.updateDetails()
	}

	this.doOnUserLogOut = function() {
		view.authPanel.slideDown();
		view.waitProgressBar.fadeOut();
		view.infolabel.text("Authorization fails! Try again...")
		view.infoPanel.slideDown().delay(800).fadeOut(400);	
	}

	this.doOnError = function(statusCode){
		view.blockPanel.fadeIn();
		view.waitProgressBar.fadeOut();
		view.infolabel.text("Error ("+statusCode+") ! Please try again later...")
		view.infoPanel.slideDown().delay(800).fadeOut(400);	
		view.authPanel.slideDown("slow");		
	}

	this.doOnDetailsUpdated = function(){
		this.updateAwakeSecondsUI(model.awakeMinutes);
		view.waitProgressBar.fadeOut("fast");
		view.blockPanel.fadeOut();
		
	}

	this.doOnAwakeFetch = function(value){
		this.updateAwakeSecondsUI(model.awakeMinutes);	
	}
	
}