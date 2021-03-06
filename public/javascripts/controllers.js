
app.controller('LoginController', function($scope, $location, $window) {
    this.setAvatar = function(avatar) {
        $window.localStorage.setItem("avatar", avatar)
    }
});

app.controller("ChatController", function ($scope, $compile, $window, $document) {
        var chat = this;
        chat.topic = ""

        $scope.$watch("username", function () {
            chat.username = $scope.username;
        });

        $scope.$watch("topic", function () {
            chat.topic = $scope.topic;
        });

        $window.onload = function() {
            chat.initWS(chat.topic)
        }

        chat.initWS = function (topic) {
            chat.ws = new WebSocket("ws://localhost:9000/socket/" + topic);
            chat.ws.onmessage = function (msg) {
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
                            var tMsg = {"username": null, "data": data.username + " created topic '"+data.data+"'"}
                            chat.messages.push(tMsg)
                        }
                        break;
                    case messages.userLeftChat:
                    case messages.disconnected:
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
                        break;
                    case messages.setAvatar:
                        if (chat.topic == data.topic) {
                            var avatar = JSON.stringify(data.data)
                            //TODO: write logic
                        }
                        break;
                }

                $scope.$digest();
            }
        }

        chat.sendFirstMsg = function() {
            var msg = {"type": messages.userJoinedChat, "username": chat.username, "topic": chat.topic};
            chat.ws.send(JSON.stringify(msg))
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
        chat.avatar = {}

        $scope.putMessage = function (text, user, date) {
            var tMsg = {"username": user, "data": text, "date": date}
            tMsg["firstClass"] = chat.getClassToAdd(tMsg)
            chat.messages.push(tMsg)
            chat.topicParticipants.push(tMsg.username)
        }

        chat.sendMessage = function () {
            var msg = {
                "type": messages.message,
                "username": chat.username,
                "date": new Date(),
                "data": chat.currentMessage,
                "topic": chat.topic,
                "id": guid()
            }
            chat.messages.push(msg);
            chat.currentMessage = "";
            chat.ws.send(JSON.stringify(msg));
        };

        chat.disconnect = function () {
            var msg = {
                "type": messages.disconnected,
                "username": chat.username,
                "topic": chat.topic,
                "id": guid()
            }
            chat.ws.send(JSON.stringify(msg));
        }

        chat.leave = function () {
            var msg = {
                "type": messages.userLeftChat,
                "username": chat.username,
                "topic": chat.topic,
                "id": guid()
            }
            chat.ws.send(JSON.stringify(msg));
            $window.location.href = 'http://localhost:9000/joinChat?username=' + chat.username
        }

         $scope.pushTopic = function(topic) {
            if ($.inArray(topic, chat.topics) == -1)
              chat.topics.push(topic)
        }

        chat.deleteTopic = function (topicName) {
            chat.topics.splice(chat.topics.indexOf(topicName))

            var msg = {
                "type": messages.deleteTopic,
                "username": chat.username,
                "topic": chat.topic,
                "data": topicName,
                "id": guid()
            }
            chat.ws.send(JSON.stringify(msg));
            $scope.$digest()
        }

        chat.getAvatar = function() {
            return $window.localStorage.getItem("avatar")
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
                "id": guid()
            }
            chat.ws.send(JSON.stringify(msg));
        }

        chat.createTopic = function () {
            var topic = chat.newTopic.replace(/ /g, "\u00A0")
            chat.topics.push({"name": topic, "count": 0})

            var msg = {
                "type": messages.topic,
                "data": topic,
                "username": chat.username,
                "topic": chat.topic,
                "id": guid()
            }
            chat.ws.send(JSON.stringify(msg))
            chat.newTopic = "";
        }

    });