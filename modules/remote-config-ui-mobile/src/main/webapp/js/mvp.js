function initMVP(){
    var model = Object.create(ModelPrototype);
    var presenter = Object.create(PresenterPrototype)
    presenter.constructor(model,{
        _typeName:"View",
        loginBtn:$("#login-btn"),
        userNameInput:$("#user-input"),
        passInput:$("#pass-input"),
        authDialog:$("#loginDialog"),
        infolabel:$("#info-label"),
        awakeSleep:$("#awake-flip"),
        awakeMinutesLabel:$("#awake-minutes-label"),
        awakeMinutesSlider:$("#awake-second-slider"),
        lastOnlideDateLabel:$("#last-online-date-label"),
        statusLabel:$("#status-label"),
        offlineTillDateLabel:$("#offline-till-date-label")
    });
    model.constructor(presenter);
    presenter.doOnStartup();
}