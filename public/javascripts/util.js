Array.prototype.getUnique = function () {
    var u = {}, a = [];
    for (var i = 0, l = this.length; i < l; ++i) {
        if (u.hasOwnProperty(this[i])) {
            continue;
        }
        a.push(this[i]);
        u[this[i]] = 1;
    }
    return a;
}

var getClassValue = function (elemId, sameAsPrev, firstClass, secondClass, checkClass) {
    var elem = angular.element(document.querySelector(elemId));
    if (sameAsPrev)
        if (elem.hasClass(checkClass))
            return firstClass;
        else
            return secondClass
    if (!sameAsPrev)
        if (elem.hasClass(checkClass))
            return secondClass;
        else
            return firstClass
}

