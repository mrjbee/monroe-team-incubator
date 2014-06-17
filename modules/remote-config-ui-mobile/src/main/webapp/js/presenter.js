
var PresenterPrototype = {

    _lastMinutes: -1,
    _model: null,
    _view: null,

    constructor:function _constructor(model,view){
        this._model = model;
        this._view = view;

        this._view.loginBtn.click(function(){
            this.doOnLoggingBtnClick();
        }.bind(this));
        this._view.awakeMinutesSlider.parent().change(function() {
            var minutesValue = this._view.awakeMinutesSlider.val();
            this.updateAwakeSecondsUI(minutesValue);
        }.bind(this));
        this._view.awakeMinutesSlider.slider({
            stop: function( event, ui ) {
                var minutesValue = this._view.awakeMinutesSlider.val();
                this._model.saveAwakeSeconds(minutesValue);
            }.bind(this)
        });
        this._view.awakeSleep.bind( "change", function(event, ui) {
            if (this._view.awakeSleep.val()=="on"){
                minutesValue = this._view.awakeMinutesSlider.val();
                this._model.saveAwakeSeconds(minutesValue);
            } else {
                this._model.saveAwakeSeconds(0);
            }
        }.bind(this));
    },

    updateAwakeSecondsUI : function(minutes){

        if (this._lastMinutes == minutes) return;

        this._lastMinutes = minutes;

        if(minutes == 0){
            this._view.awakeMinutesLabel.text("stay up");
            this._view.awakeMinutesSlider.slider("disable");
            this._view.awakeSleep.val("off");
            this._view.awakeSleep.flipswitch( "refresh" );
        } else {
            hr = Math.floor(minutes/60);
            mins= minutes % 60;
            if (hr == 0){
                this._view.awakeMinutesLabel.text(mins+"min");
            } else {
                this._view.awakeMinutesLabel.text(hr+" hr "+mins+"min");
            }
            this._view.awakeMinutesSlider.slider("enable");
            this._view.awakeMinutesSlider.val(minutes);
            this._view.awakeMinutesSlider.slider("refresh");
            this._view.awakeSleep.val("on");
            this._view.awakeSleep.flipswitch( "refresh" );
        }
    },

    doOnStartup : function(){
        this._view.authDialog.popup("open");
    },

    doOnLoggingBtnClick : function(){
        var userNameTxt = this._view.userNameInput.val();
        var passwordTxt = this._view.passInput.val();
        this._view.authDialog.popup("close");
        var loginRequestModel = {
            userName:userNameTxt,
            password:passwordTxt
        };
        this._model.loginUser(loginRequestModel);
    },

    doOnUserLogIn : function() {
        this._model.updateDetails();
    },

    doOnUserLogOut : function() {
        this._view.authDialog.popup("open");
        //this._view.waitProgressBar.fadeOut();
        this._view.infolabel.text("Authorization fails! Try again...")
        this._view.infolabel.slideDown().delay(800).fadeOut(400);
    },

    doOnError : function(statusCode){
        this._view.infolabel.text("Error ("+statusCode+") ! Please try again later...")
        this._view.infolabel.slideDown().delay(800).fadeOut(400);
        this._view.authDialog.popup("open");
    },

    doOnDetailsUpdated : function(){
        this.updateAwakeSecondsUI(this._model.awakeMinutes)
        this._view.statusLabel.text(this._model.lastStatus)
        this._view.lastOnlineDateLabel.text(this._model.lastDate)
        this._view.offlineTillDateLabel.text(this._model.offlineTillDate)
    },

    doOnAwakeFetch : function(value){
        this.updateAwakeSecondsUI(this._model.awakeMinutes);
    }
}