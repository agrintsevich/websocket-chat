app.directive('topic', function () {
        return {
            require: "^ngController",
            scope: {
                text: '@'
            },
            controller: function ($scope, $attrs) {
                $scope.getTopicName = function (data) {
                    var jsonTopic
                    try {
                        jsonTopic = JSON.parse(data)
                    } catch (e) {
                        jsonTopic = data
                    }
                    if (jsonTopic && jsonTopic.data) {
                        return jsonTopic.data
                    }
                    else return jsonTopic;
                }
            },

            template: "<span class='topic'> {{getTopicName(getTopicName(text))}} <a class='subscribeTopic' id='subscribeTopic'> [subscribe] </a></span>",
            link: function (scope, elem, attrs, ngCtrl) {
                var topicName = scope.getTopicName(scope.text)
                elem.on("click", function () {
                    ngCtrl.subscribeToTopic(topicName)
                    elem.html("<span class='topicSubscripedLi'><a class='subscribedTopicHref'>" + topicName + "<a/></span>")

                    elem.on('click', function () {
                        ngCtrl.goToTopic(topicName)
                    })
                })
            }
        };
    });
    app.directive("fileread", function () {
        return {
            require: "^ngController",
            scope: {
                fileread: "="
            },
            link: function (scope, element, attributes, ngCtrl) {
                element.bind("change", function (changeEvent) {
                    var reader = new FileReader();
                    reader.onload = function (loadEvent) {
                        scope.$apply(function () {
                            scope.fileread = loadEvent.target.result;
                            ngCtrl.setAvatar(scope.fileread)
                        });
                    }
                    reader.readAsDataURL(changeEvent.target.files[0]);
                });
            }
        }
    });