if (window.console) {
    console.log("Welcome to chat!");
}

angular.module("ChatApp", [])
    .controller("ChatController", function ($scope, $compile, $window, $document) {

        var chat = this;
        chat.topic = ""
        chat.oldMessages = []

        $scope.$watch("username", function () {
            chat.username = $scope.username;
        });

        $scope.$watch("topic", function () {
            chat.topic = $scope.topic;
            $scope.initWS(chat.topic)
        });

        $scope.guid = function () {
            function s4() {
                return Math.floor((1 + Math.random()) * 0x10000)
                    .toString(16)
                    .substring(1);
            }

            return s4() + s4() + '-' + s4() + '-' + s4() + '-' +
                s4() + '-' + s4() + s4() + s4();
        }

        $scope.initWS = function (topic) {
            $scope.ws = new WebSocket("ws://localhost:9000/socket/" + topic);

            $scope.ws.onmessage = function (msg) {

                var data = JSON.parse(msg.data)
                switch (data.type) {
                    case messages.userJoinedChat:
                        if (chat.topic == data.topic) {
                            var tMsg = {"username": null, "data": data.username + " has joined chat"}
                            chat.messages.push(tMsg)
                            chat.topicParticipants.push(data.username)
                        }
                        break;
                    case messages.message:
                        if (chat.topic == data.topic) {
                            data["firstClass"] = chat.getClassToAdd(data)
                            chat.messages.push(data);
                            chat.topicParticipants.push(data.username)
                        }
                        break;
                    case messages.topic:
                        if (chat.topic == data.topic) {
                            chat.topics.push(data)
                        }
                        break;
                    case messages.userLeftChat:
                        if (chat.topic == data.topic) {
                            var index = chat.topicParticipants.indexOf(data.username)
                            chat.topicParticipants.splice(index)
                            var tMsg = {"username": null, "data": data.username + " has left"}
                            chat.messages.push(tMsg)
                        }
                        break;
                    case messages.deleteTopic:
                        if (chat.topic == data.topic) {
                            var topicName = JSON.stringify(data.data)
                            chat.topics.splice(chat.topics.indexOf(topicName))
                        }
                        break
                }
                $scope.$apply();
                $scope.$digest();
            }
            $scope.ws.onopen = function () {
                var msg = {"type": messages.userJoinedChat, "username": chat.username, "topic": chat.topic};
                $scope.ws.send(JSON.stringify(msg))
            }
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

        chat.getClassToAdd = function (data) {
            var classToAdd = "R"
            if (chat.messages.length) {
                if (chat.messages[chat.messages.length - 1].username == data.username) {

                    var lastId1 = '#first' + chat.messages[chat.messages.length - 1].id
                    classToAdd = getClassValue(lastId1, true, "R", "L", "R")
                } else {
                    classToAdd = getClassValue(lastId1, false, "R", "L", "L")
                }
            } else {
                classToAdd = "L"
            }
            return classToAdd
        }
        chat.messages = [];
        chat.userTopics = [];
        chat.topics = [];
        chat.currentMessage = "";
        chat.username = "";
        chat.topic = ""
        chat.topicParticipants = []
        chat.newTopic = "";

        $scope.putMessage = function (text, user, date) {
            var tMsg = {"username": user, "data": text, "date": date}
            tMsg["firstClass"] = chat.getClassToAdd(tMsg)
            chat.messages.push(tMsg)
        }

        chat.sendMessage = function () {
            var msg = {
                "type": messages.message,
                "username": chat.username,
                "date": new Date(),
                "data": chat.currentMessage,
                "topic": chat.topic,
                "id": $scope.guid()
            }
            chat.messages.push(msg);
            chat.currentMessage = "";
            $scope.ws.send(JSON.stringify(msg));
        };

        chat.disconnect = function () {
            var msg = {
                "type": messages.disconnected,
                "username": chat.username,
                "topic": chat.topic,
                "id": $scope.guid()
            }
            $scope.ws.send(JSON.stringify(msg));
        }

        chat.leave = function () {
            var msg = {
                "type": messages.userLeftChat,
                "username": chat.username,
                "topic": chat.topic,
                "id": $scope.guid()
            }
            $scope.ws.send(JSON.stringify(msg));
            $window.location.href = 'http://localhost:9000/joinChat?username=' + chat.username
        }

        chat.deleteTopic = function (topicName, e) {
            var elem = angular.element(e.srcElement);
            var parentEl = angular.element(elem[0].parentNode)
            parentEl.remove()
            var msg = {
                "type": messages.deleteTopic,
                "username": chat.username,
                "topic": chat.topic,
                "data": topicName,
                "id": $scope.guid()
            }
            $scope.ws.send(JSON.stringify(msg));

        }

        this.goToTopic = function (topic) {
            $window.location.href = 'http://localhost:9000/joinChat?username=' + chat.username + '&topic=' + topic;
        }

        this.subscribeToTopic = function (topic) {
            var msg = {
                "type": messages.subscribeTopic,
                "username": chat.username,
                "topic": chat.topic,
                "data": topic,
                "id": $scope.guid()
            }
            if (typeof $scope.ws !== 'undefined')
                $scope.ws.send(JSON.stringify(msg));
        }

        chat.createTopic = function () {

            var topic = chat.newTopic.replace(/ /g, "\u00A0")
            chat.topics.push(topic)
            var msg = {
                "type": messages.topic,
                "data": topic,
                "username": chat.username,
                "topic": chat.topic,
                "id": $scope.guid()
            }
            $scope.ws.send(JSON.stringify(msg))
            chat.newTopic = "";
        }

    })
    .directive('topic', function () {
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




