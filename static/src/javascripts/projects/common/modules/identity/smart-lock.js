define([
    'bean'
    ],
    function (bean) {
        return function SmartLock()  {

            this.init = function () {

                var form = document.querySelector('.password-form');

                if (form) {
                    bean.on(form, 'submit', function () {
                        if (navigator.credentials) {
                            /*global PasswordCredential*/
                            var c = new PasswordCredential(form);
                            navigator.credentials.store(c);
                            return;
                        }
                    });
                }
        };
    };
});
