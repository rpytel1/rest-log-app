var myapp = angular.module('myapp', []);
myapp.controller('logViewerController', function ($scope, $http) {

    $scope.getNumber = function () {
        return new Array($scope.eventType);
    }

    $scope.getLogRecords = function () {
        $http.get('http://localhost:8080/logserver/webapi/logagent/' + $scope.model.logId).success(function (response) {
            $scope.listLogRecords = response.logRecord;
            $scope.eventType = response.logEventType;
            console.log($scope.eventType);
            $scope.init();
        });

    }

    $scope.init = function () {
        $scope.logrecord = {};
        console.log("init");
        $scope.logrecord.type = {};
        for (var i = 0; i < $scope.eventType; i++) {
            $scope.logrecord.type[i] = {
                descritpion: ''
            };
        }
        ;
    };
    $scope.addLogRecord = function () {
        var typelist = [];
        for (var i = 0; i < $scope.eventType; i++) {
            typelist.push($scope.logrecord.type[i].descritpion);
        }
        JSON.parse(JSON.stringify(typelist));
        console.log(typelist);
        $http.post("http://localhost:8080/logserver/webapi/logagent/" + $scope.model.logId, {
            'eventId':$scope.logrecord.eventId,
            'type': typelist

        })
            .success(function () {
                $scope.getLogRecords();
            });

    };
    $scope.deleteLogRecord = function (lr) {
        console.log(lr);
        $http.delete("http://localhost:8080/logserver/webapi/logagent/" + $scope.model.logId + "/" + lr.id).success(function () {
            $scope.getLogRecords();
        });
    }
    $scope.propertyName = 'id';
    $scope.reverse = true;
    $scope.sortBy = function (propertyName) {
        $scope.reverse = ($scope.propertyName === propertyName) ? !$scope.reverse : false;
        $scope.propertyName = propertyName;
    };
    $scope.addNewEvent = function () {
        $http.post("http://localhost:8080/logserver/webapi/logagent/" + $scope.newEvent.logId + "/newbase/" + $scope.newEvent.newSize, {});
    };


})
;
