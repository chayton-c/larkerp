function show_img(t) {
    //页面层
    layer.open({
        type: 1,
        skin: 'layui-layer-rim', //加上边框
        area: ['80%', '80%'], //宽高
        shadeClose: true, //开启遮罩关闭
        end: function (index, layero) {
            return false;
        },
        content: '<div style="text-align:center"><img src="' + $(t).attr('data-src') + '" /></div>'
    });
}

function isNum(unchecked) {
    if (!unchecked)
        return false;

    const reg = new RegExp("^([-+])?\\d+(\\.\\d+)?$")
    return reg.test(unchecked);
}

function isInteger(unchecked) {
    if (!unchecked)
        return false;

    const reg = new RegExp("^[0-9]*$");
    return reg.test(unchecked);
}


function getQueryString(name) {
    const reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
    const r = window.location.search.substr(1).match(reg);
    if (r != null)
        return unescape(r[2]);
    return null;
}