
!function() {

  var opts = {
      url : 'ws://localhost:8080/simple-ws/my-ws',
      uid : '01234'
  };

  var ws = null;

  var onopen = function(event) {
    console.log(event.type);
    send([
      {
        serviceName : '*',
        methodName : 'login',
        uid: opts.uid,
        lang: navigator.language
      },
      [{ f1: 'abc', f2: 123 }]
    ]);
  };

  var onclose = function(event) {
    console.log(event.type);

    ws = null;

    reopen();
  };

  var onmessage = function(event) {
    console.log(event.data);
    /*
    var data = JSON.parse(event.data);
    var action = (<any>actions)[data.action];
    if (action) {
      action(data);
    }*/
  };

  var onerror = function(event) {
    console.log(event.type);
  };

  var initWS = function() {
    var ws = new WebSocket(opts.url);
    ws.onopen = onopen;
    ws.onclose = onclose;
    ws.onmessage = onmessage;
    ws.onerror = onerror;
    return ws;
  };

  var reopen = function() {
    window.setTimeout(function() {
      if (navigator.onLine) {
        ws = initWS();
      } else {
        reopen();
      }
    }, 5000);
  };

  ws = initWS();

  var send = function(data) {
    if (ws == null) {
      return;
    }
    ws.send(JSON.stringify(data) );
  };

}();
