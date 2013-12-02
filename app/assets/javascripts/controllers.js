'use strict';

angular.module('realtimeSearch.controllers', []).
    controller('SearchCtrl', function ($scope) {
        $scope.searchResults = [];
        $scope.searchString = "";

        $scope.addSearchResult = function (e) {
            $scope.$apply(function () {
            	var raw = JSON.parse(e.data);
            	var target = raw.target;
            	var data = raw.data;
            	if (target == "searchResult") {
            		$scope.searchResults.unshift(data);
            	} else { 
            		// 
            	}
            });
        }

        $scope.startSearching = function () {
            $scope.stopSearching()
            $scope.searchResults = [];
            $scope.searchFeed = new EventSource("/search/" + $scope.searchString);
            $scope.searchFeed.addEventListener("message", $scope.addSearchResult, false);
        };

        $scope.stopSearching = function () {
            if (typeof $scope.searchFeed != 'undefined') {
                $scope.searchFeed.close();
            }
        }
    });