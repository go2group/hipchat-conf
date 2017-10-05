AJS.$(function () {
    var $ = jQuery;

    $('body').bind('ajaxComplete', function (e, x) {
        if (x.responseText && /vcard/.test(x.responseText)) {
            var vcards = $('.vcard');
            $.each(vcards, function (i, vcard) {
                var vcard = $(vcard),
                    email = $('a.email', vcard).html();
                if (!vcard.hasClass('hc-status-applied')) {
                    $.get(AJS.Data.get("context-path") + '/rest/hipchatproxy/1/user/show?user_id=' + encodeURIComponent(email), function (d) {
                        vcard.addClass('hc-status-applied');
                        $('div.values', vcard)
                            .append('<a class="hc-status ' + d.user.status.toLowerCase() + '" title="'
                                + d.user.status + ' on HipChat"><span>'
                                + d.user.status + '</span></a>');
                    }, 'json');
                }
            });

        }
    });

});