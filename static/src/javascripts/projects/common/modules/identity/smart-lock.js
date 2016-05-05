define([
    'bean'
    ],
    function (bean) {
        return function SmartLock()  {

            this.init = function () {

                const form = document.querySelector('.password-form');

                if (form) {
                    bean.on(form, 'submit', function () {
                        if (navigator.credentials) {
                            var c = new PasswordCredential(form);
                            navigator.credentials.store(c);
                            return;
                        }
                    });
                }
        };
    };
});
