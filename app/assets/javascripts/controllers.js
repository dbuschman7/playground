'use strict';

angular.module('realtimeSearch.controllers', []).controller(
		'SearchCtrl',

		function($scope) {
			$scope.searchResults = [];
			$scope.searchString = "";
			$scope.serverTime = "Nothing Here Yet";

			$scope.data = [ 10, 20, 30, 40 ];

			// browsers
			$scope.safari = 0;
			$scope.chrome = 0;
			$scope.firefox = 0;
			$scope.other = 0;

			// devices
			$scope.desktop = 0;
			$scope.tablet = 0;
			$scope.phone = 0;
			$scope.tv = 0;

			// throughput
			$scope.requests = 0;
			$scope.responseTime = 0;

			$scope.handleServerEvent = function(e) {
				$scope.$apply(function() {
					var raw = JSON.parse(e.data);
					var target = raw.target;
					var data = raw.data;
					// console.log("Received data for " + target);
					if (target == "searchResult") {
						$scope.searchResults.unshift(data)
					} else if (target == "serverTick") {
						$scope.serverTime = data;
					} else if (target == "statistics") {
						$scope.data.shift();
						$scope.data.shift();
						$scope.data.shift();
						$scope.data.shift();
						$scope.data.push(data.GET);
						$scope.data.push(data.PUT);
						$scope.data.push(data.POST);
						$scope.data.push(data.DELETE);
						redrawMethods($scope.data);

						// browser data
						$scope.safari = data.Safari;
						$scope.chrome = data.Chrome;
						$scope.firefox = data.Firefox;
						$scope.other = data.IE + data.HttpClient;
						$scope.highBrand = determineHighBrand($scope);

						// device data
						$scope.desktop = data.Desktop;
						$scope.tablet = data.Tablet;
						$scope.phone = data.Phone;
						$scope.tv = data.TV;
						$scope.highDevice = determineHighDevice($scope);

						// response time
						$scope.requests = data.requests;
						$scope.responseTime = data.totalResponseTime
								/ data.requests;
					}
				});
			}

			$scope.startSearching = function() {
				$scope.stopSearching()
				$scope.searchResults = [];
				$scope.searchFeed = new EventSource("/search/"
						+ $scope.searchString);
				$scope.searchFeed.addEventListener("message",
						$scope.handleServerEvent, false);
			};

			$scope.stopSearching = function() {
				if (typeof $scope.searchFeed != 'undefined') {
					$scope.searchFeed.close();
				}
			}
		});

//