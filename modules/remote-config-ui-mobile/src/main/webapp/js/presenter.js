function Presenter(model, view){

	this.typeName = "Presenter"
	
	this.model = model;
	this.view = view;
	this.lastMinutes = -1;
	thisPresentor = this


	this.updateAwakeSecondsUI = function(minutes){
		if (this.lastMinutes == minutes) return
		this.lastMinutes = minutes
		if(minutes == 0){
			view.awakeMinutesLabel.text("stay up")
			view.awakeMinutesSlider.slider("disable")
			view.awakeSleep.val("off")
			view.awakeSleep.flipswitch( "refresh" )
		} else {
			hr = Math.floor(minutes/60);
			mins= minutes % 60 
			if (hr == 0){
				view.awakeMinutesLabel.text(mins+"min")
			} else {
				view.awakeMinutesLabel.text(hr+" hr "+mins+"min")
			}
			view.awakeMinutesSlider.slider("enable")
			view.awakeMinutesSlider.val(minutes)
			view.awakeMinutesSlider.slider("refresh")
			view.awakeSleep.val("on")
			view.awakeSleep.flipswitch( "refresh" )
		}		
	}

	this.doOnStartup = function(){
		model.setPresenter(this)
		view.loginBtn.click(this.doOnLoggingBtnClick)
		
		view.authDialog.popup("open");
		view.awakeMinutesSlider.parent().change(function() {
  			minutesValue = view.awakeMinutesSlider.val()
			thisPresentor.updateAwakeSecondsUI(minutesValue);
        }); 

		view.awakeMinutesSlider.slider({
			stop: function( event, ui ) {
				minutesValue = view.awakeMinutesSlider.val()
				thisPresentor.model.saveAwakeSeconds(minutesValue);				
			}
		});

		view.awakeSleep.bind( "change", function(event, ui) {
  			if (view.awakeSleep.val()=="on"){
  				minutesValue = view.awakeMinutesSlider.val()
				thisPresentor.model.saveAwakeSeconds(minutesValue);				  				
  			} else {
  				thisPresentor.model.saveAwakeSeconds(0);				  				
  			}
  		});

	}

	this.doOnLoggingBtnClick = function(){
		var userNameTxt = view.userNameInput.val();
		var passwordTxt = view.passInput.val();
		view.authDialog.popup("close");
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
		view.authDialog.popup("open");
		//view.waitProgressBar.fadeOut();
		view.infolabel.text("Authorization fails! Try again...")
		view.infolabel.slideDown().delay(800).fadeOut(400);	
	}

	this.doOnError = function(statusCode){
		//view.blockPanel.fadeIn();
		//view.waitProgressBar.fadeOut();
		view.infolabel.text("Error ("+statusCode+") ! Please try again later...")
		view.infolabel.slideDown().delay(800).fadeOut(400);	
		view.authDialog.popup("open");		
	}

	this.doOnDetailsUpdated = function(){
		this.updateAwakeSecondsUI(model.awakeMinutes)
		view.statusLabel.text(model.lastStatus)
		view.lastOnlideDateLabel.text(model.lastDate)
		view.offlineTillDateLabel.text(model.offlineTillDate)
		//view.waitProgressBar.fadeOut("fast");
		//view.blockPanel.fadeOut();
	}

	this.doOnAwakeFetch = function(value){
		this.updateAwakeSecondsUI(model.awakeMinutes);	
	}
	
}