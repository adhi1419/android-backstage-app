var database = firebase.database();
var currentUserId = "";
var indexPath = "/C:/Users/Shivin/Downloads/template2/index.html";
/*var eventRef = database.ref('Event');
var inventoryRef = database.ref('Inventory');
var usersRef = database.ref('Users');*/

window.onload = init();

function init() {

    var loginFormDiv = document.getElementById('loginFormDiv');
    var tabsBar = document.getElementById('tabsBar');
    var loginValueRef = database.ref('loginValue').once('value', function(snapshot) {});
    var currentUserRef = firebase.auth().currentUser;

    firebase.auth().onAuthStateChanged(function(user) {
        if (user) {
            // User is signed in.
            loginFormDiv.style.display = "none";
            tabsBar.style.display = "block";
            homeButtonClick();
        } else {
            // No user is signed in.
            //logging in
            var loginButton = document.getElementById('loginButton');
            var loginPassword = document.getElementById('loginPassword');
            loginButton.onclick = loginButtonClick;
            loginPassword.onkeypress = function(eventObj) {
                enterKeyPress(eventObj, loginButtonClick);
            };

            function loginButtonClick() {

                var loginEmailValue = document.getElementById('loginEmail').value;
                var loginPasswordValue = document.getElementById('loginPassword').value;
                var loginText = document.getElementById('loginText');

                loginEmailValue += "@goa.bits-pilani.ac.in";

                firebase.auth().signInWithEmailAndPassword(loginEmailValue, loginPasswordValue).then(function(snapshot) {
                        //logged in
                        tabsBar.style.display = "block";
                        loginFormDiv.style.display = "none";
                        homeButtonClick();

                    },
                    function(error) {
                        var errorMessage = error.message;
                        loginText.innerHTML = errorMessage;
                    });
            }
        }
    });



    var eventsButton = document.getElementById('eventsButton');
    eventsButton.onclick = eventsButtonClick;
    var inventoryButton = document.getElementById('inventoryButton');
    inventoryButton.onclick = inventoryButtonClick;
    var membersButton = document.getElementById('membersButton');
    membersButton.onclick = membersButtonClick;
    var homeButton = document.getElementById('homeButton');
    homeButton.onclick = homeButtonClick;
    var logOutButton = document.getElementById('logOutButton');
    logOutButton.onclick = logOutButtonClick;
    //more tab onclick listeners come here

}

function toDate(date) {
    var pos = date.indexOf('T');
    var finalDate = date.substring(0, pos) + " " + date.substring(pos + 1, date.length) + ":00";
    return (finalDate);
}

function enterKeyPress(eventObj, buttonFunction) {
    if (eventObj.keyCode == 13)
        buttonFunction();
}


function tabButtonClick(currTab) {
    var tabs = ["homeDiv", "eventDiv", "membersDiv", "inventoryDiv"];
    for (var i = 0; i < tabs.length; i++) {
        if (tabs[i] == currTab) {
            var currentTab = document.getElementById(currTab);
            currentTab.style.display = "block";
        } else {
            var otherTab = document.getElementById(tabs[i]);
            otherTab.style.display = "none";

            tabClone = otherTab.cloneNode(true);
            otherTab.parentNode.replaceChild(tabClone, otherTab);
        }
    }
}

function logOutButtonClick() {
    firebase.auth().signOut().then(function() {
        location.reload();
    }, function(error) {
        console.log(error.message);
    });
}


function homeButtonClick() {

    tabButtonClick("homeDiv");

    firebase.auth().currentUser.providerData.forEach(function(profile) {
        currentUserId = profile.email.substring(0, 8);
    });

    //setting user profile
    var homeBlockHeading = document.getElementById('homeBlockHeading');
    var homeBlockContent = document.getElementById('homeBlockContent');
    database.ref('Users').once('value', function(snapshot) {

        var currentUser = snapshot.val()[currentUserId];
        var currentUserPosition;
        switch (currentUser.role) {
            case 0:
                currentUserPosition = "AVU Unit/Faculty";
                break;
            case 1:
                currentUserPosition = "Co-ordinator";
                break;
            case 2:
                currentUserPosition = "Core Member";
                break;
            case 3:
                currentUserPosition = "Crew Member";
                break;
            default:
                currentUserPosition = "";
        }
        var str = currentUserPosition + "<br/>" + currentUserId;
        homeBlockHeading.innerHTML = currentUser.name;
        homeBlockContent.innerHTML = str;
    });


}

function eventsButtonClick() {

    tabButtonClick("eventDiv");

    //add event modal comes here
    var addEventModal = document.getElementById('addEventModal');
    var addEventButton = document.getElementById("addEventButton");
    // Get the <span> element that closes the modal
    var span = document.getElementsByClassName("close")[0];
    // When the user clicks on the button, open the modal
    addEventButton.onclick = function() {
            database.ref('Users/' + currentUserId).once('value', function(snapshot) {
                var currentUserRole = snapshot.val().role;
                if (currentUserRole == 1) {
                    addEventModal.style.display = "block";
                    var validateEventButton = document.getElementById('validateEventButton');
                    validateEventButton.onclick = function() {
                        var eventExitTime = toDate(document.getElementById('eventExitTime').value);
                        var eventEntryTime = toDate(document.getElementById('eventEntryTime').value);
                        var eventEntry = {
                            end: eventExitTime,
                            name: document.getElementById('eventName').value,
                            place: document.getElementById('eventVenue').value,
                            start: eventEntryTime
                        };
                        database.ref('Event').push(eventEntry);
                        addEventModal.style.display = "none";
                    }
                } else {
                    var addEventButtonText = document.getElementById('addEventButtonText');
                    addEventButtonText.innerHTML = "You are not a Co-ordinator";
                    setTimeout(function() {
                        addEventButtonText.innerHTML = "";
                    }, 2000);
                }
            });
        }
        // When the user clicks on <span> (x), close the modal
    span.onclick = function() {
            addEventModal.style.display = "none";
        }
        // When the user clicks anywhere outside of the modal, close it
    window.onclick = function(event) {
        if (event.target == addEventModal) {
            addEventModal.style.display = "none";
        }
    }


    database.ref('Event').on('value', function(snapshot) {
        var eventList = document.getElementById("eventListTable");
        var data = snapshot.val();
        var str = '<thead><th colspan="4">EVENTS LIST</th></thead><th>Event Name</th> <th> Venue </th> <th> Entry Time </th> <th>Exit Time</th>';
        for (var key in data) {
            if (data.hasOwnProperty(key)) {
                var buttonId = key + 'button';
                //converting firebase data to html
                str += "<tr> <td>" + data[key].name + "</td><td>" + data[key].place + "</td><td>" + data[key].start + "</td><td>" + data[key].end + '</td><td><button class="buttonA" id="' + buttonId + '">X</button></td></tr>';
            }
        }
        eventList.innerHTML = str;
        //setting listeners for delete event button
        for (var key in data) {
            var buttonId = key + 'button';
            var button = document.getElementById(buttonId);
            button.onclick = deleteEventClick;
        }
    });

    function deleteEventClick(event) {
        database.ref('Users/' + currentUserId).once('value', function(snapshot) {
            var currentUserRole = snapshot.val().role;
            if (currentUserRole == 1) {
                var button = event.target;
                var eventId = button.id.substring(0, button.id.length - 6);
                var eventRemoveRef = database.ref('Event/' + eventId);
                eventRemoveRef.remove();
            } else {
                var addEventButtonText = document.getElementById('addEventButtonText');
                addEventButtonText.innerHTML = "You are not a Co-ordinator";
                setTimeout(function() {
                    addEventButtonText.innerHTML = "";
                }, 2000);
            }
        });
    }

}

function inventoryButtonClick() {

    tabButtonClick('inventoryDiv');

    //add item modal comes here
    var addItemModal = document.getElementById('addItemModal');
    var addItemButton = document.getElementById("addItemButton");
    // Get the <span> element that closes the modal
    var span = document.getElementsByClassName("close")[1];
    // When the user clicks on the button, open the modal
    addItemButton.onclick = function() {

            var eventSelect = document.getElementById('eventSelect');
            var str = '';

            var eventListRef = database.ref('Event');
            eventListRef.once("value").then(function(snapshot) {
                var eventList = snapshot.val();
                for (var key in eventList) {
                    str += '<option id="' + key + 'option">' + eventList[key].name + '</option>';
                }
                eventSelect.innerHTML = str;
            });
            addItemModal.style.display = "block";
            var validateAddItemButton = document.getElementById('validateAddItemButton');
            validateAddItemButton.onclick = function(eventObj) {

                var currentdate = new Date();
                var datetime = currentdate.getFullYear() + "-" + (currentdate.getMonth() + 1) + "-" + currentdate.getDate() + " " +
                    currentdate.getHours() + ":" + currentdate.getMinutes() + ":" + currentdate.getSeconds();
                var selOptionId = eventSelect.options[eventSelect.selectedIndex].id;
                var eventKey = selOptionId.substring(0, selOptionId.length - 6);
                var itemEntry = {
                    event: eventKey,
                    id: currentUserId,
                    item: document.getElementById('addItemId').value,
                    status: "0",
                    time: datetime
                };
                database.ref('Inventory').push(itemEntry);
                addItemModal.style.display = "none";
            }
        }
        // When the user clicks on <span> (x), close the modal
    span.onclick = function() {
            addItemModal.style.display = "none";
        }
        // When the user clicks anywhere outside of the modal, close it
    window.onclick = function(event) {
        if (event.target == addItemModal) {
            addItemModal.style.display = "none";
        }
    }

    //return item modal comes here
    var returnItemModal = document.getElementById('returnItemModal');
    var returnItemButton = document.getElementById("returnItemButton");
    // Get the <span> element that closes the modal
    var span = document.getElementsByClassName("close")[2];
    // When the user clicks on the button, open the modal
    returnItemButton.onclick = function() {

            returnItemModal.style.display = "block";
            var validateReturnItemButton = document.getElementById('validateReturnItemButton');
            validateReturnItemButton.onclick = function(eventObj) {
                var returnItemId = document.getElementById('returnItemId').value;
                var inventoryListRef = database.ref('Inventory');
                inventoryListRef.once("value").then(function(snapshot) {
                    var inventoryList = snapshot.val();
                    for (var key in inventoryList) {
                        if (inventoryList[key].item == returnItemId) {
                            var currentdate = new Date();
                            var datetime = currentdate.getFullYear() + "-" + (currentdate.getMonth() + 1) + "-" + currentdate.getDate() + " " +
                                currentdate.getHours() + ":" + currentdate.getMinutes() + ":" + currentdate.getSeconds();
                            var itemEntry = inventoryList[key];
                            itemEntry.status = datetime;
                            database.ref('Inventory/' + key).update(itemEntry);
                            break;
                        }
                    }
                });

                returnItemModal.style.display = "none";
            }
        }
        // When the user clicks on <span> (x), close the modal
    span.onclick = function() {
            returnItemModal.style.display = "none";
        }
        // When the user clicks anywhere outside of the modal, close it
    window.onclick = function(event) {
        if (event.target == returnItemModal) {
            returnItemModal.style.display = "none";
        }
    }

    database.ref('Inventory').on('value', function(snapshot) {
        var inventoryList = snapshot.val();

        database.ref('Event').once('value', function(snapshot) {
            var eventList = snapshot.val();

            database.ref('Users').once('value', function(snapshot) {
                var usersList = snapshot.val();

                var fullInventoryListButton = document.getElementById('fullInventoryListButton');
                fullInventoryListButton.onclick = fullInventoryListButtonClick;
                var eventInventoryListButton = document.getElementById('eventInventoryListButton');
                eventInventoryListButton.onclick = eventInventoryListButtonClick;
                var eventInventoryList = document.getElementById('eventInventoryList');
                var inventoryListTable = document.getElementById('inventoryListTable');

                function fullInventoryListButtonClick(eventObj) {
                    eventInventoryList.style.display = "none";
                    displayInventoryList(eventObj.target);
                }

                function eventInventoryListButtonClick() {
                    var str = "";
                    var buttonId = "";
                    for (var key in eventList) {

                        buttonId = key + "showButton";
                        str += '<button type="button" class="buttonA" id="' + buttonId + '">' +
                            eventList[key].name + ', ' + eventList[key].place + '</button></br>';
                    }
                    eventInventoryList.innerHTML = str;
                    eventInventoryList.style.display = "block";
                    inventoryListTable.style.display = "none";

                    for (var key in eventList) {
                        var buttonId = '' + key + 'showButton';
                        var button = document.getElementById(buttonId);

                        button.onclick = function(eventObj) {
                            for (var key in eventList) {
                                var buttonId = '' + key + 'showButton';
                                if (eventObj.target.id == buttonId) {
                                    eventObj.target.style.fontWeight = 'bold';
                                    eventObj.target.style.color = '#C8C8C8';
                                } else {
                                    var button = document.getElementById(buttonId);
                                    button.style.fontWeight = 'normal';
                                    button.style.color = '#FFFFFF';
                                }
                            }
                            displayInventoryList(eventObj.target);
                        };

                    }

                }

                function displayInventoryList(eventObjTarget) {

                    var str = '<thead><th colspan="5"> INVENTORY LIST </th></thead><th>Item Number</th> <th>Event</th> <th>Issued To</th> <th>Issue Time</th> <th>Return Time</th>';
                    var noItemsInEvent = true;

                    for (var key in inventoryList) {
                        if (inventoryList.hasOwnProperty(key)) {
                            //converting firebase data to html
                            var buttonId = '' + key + 'button';
                            var userName = usersList[inventoryList[key].id].name;
                            var eventKey = inventoryList[key].event;
                            var buttonText = "";
                            var returnText = "";
                            var displayType = "";

                            if (inventoryList[key].status == "0") {
                                buttonText = "Return";
                                returnText = "Not Returned.\n Expected by " + eventList[eventKey].end;
                            } else {
                                buttonText = "x";
                                returnText = "Returned at " + inventoryList[key].status;
                            }

                            if (eventObjTarget.id == "fullInventoryListButton") {
                                displayType = "all";
                            } else {
                                displayType = eventObjTarget.id.substring(0, eventObjTarget.id.length - 10);
                            }

                            if (displayType == eventKey) {
                                noItemsInEvent = false;
                                str += "<tr> <td>" + inventoryList[key].item + "</td><td>" + eventList[eventKey].name + "," + eventList[eventKey].place +
                                    "</td><td>" + userName + "</td><td>" + inventoryList[key].time + "</td><td>" + returnText +
                                    '</td><td><button class="buttonA" id="' + buttonId + '">' + buttonText + '</button></td></tr>';
                            } else if (displayType == "all") {
                                noItemsInEvent = false;
                                str += "<tr> <td>" + inventoryList[key].item + "</td><td>" + eventList[eventKey].name + "," + eventList[eventKey].place +
                                    "</td><td>" + userName + "</td><td>" + inventoryList[key].time + "</td><td>" + returnText +
                                    '</td><td><button class="buttonA" id="' + buttonId + '">' + buttonText + '</button></td></tr>';
                            }

                        }
                    }
                    if (noItemsInEvent) {
                        str = "<p>No Items In Event</p>";
                    }
                    inventoryListTable.innerHTML = str;
                    inventoryListTable.style.display = "table";

                    //setting listeners for delete/return event button
                    for (var key in inventoryList) {

                        var eventKey = inventoryList[key].event;
                        var displayType = "";
                        if (eventObjTarget.id == "fullInventoryListButton") {
                            displayType = "all";
                        } else {
                            displayType = eventObjTarget.id.substring(0, eventObjTarget.id.length - 10);
                        }

                        var buttonId = '' + key + 'button';
                        if (displayType == eventKey) {
                            var button = document.getElementById(buttonId);
                            button.onclick = deleteOrReturnItemClick;
                        } else if (displayType == "all") {
                            var button = document.getElementById(buttonId);
                            button.onclick = deleteOrReturnItemClick;
                        }

                    }

                    //listener for deleting/returning item
                    function deleteOrReturnItemClick(eventObj) {
                        var buttonId = eventObj.target.id;
                        var itemId = buttonId.substring(0, buttonId.length - 6);
                        var itemEntry = inventoryList[itemId];
                        if (itemEntry.status == "0") {
                            //return item code
                            var currentdate = new Date();
                            var datetime = currentdate.getFullYear() + "-" + (currentdate.getMonth() + 1) + "-" + currentdate.getDate() + " " +
                                currentdate.getHours() + ":" + currentdate.getMinutes() + ":" + currentdate.getSeconds();
                            itemEntry.status = datetime;
                            database.ref('Inventory/' + itemId).update(itemEntry);
                        } else {
                            //delete item code
                            var itemRemoveRef = database.ref('Inventory/' + itemId);
                            itemRemoveRef.remove();
                        }
                    }
                }

                if (eventInventoryList.style.display == "none") {
                    displayInventoryList(fullInventoryListButton);
                } else {
                    for (var key in eventList) {
                        var buttonId = '' + key + 'showButton';
                        var button = document.getElementById(buttonId);
                        if (button != null) {
                            if (button.style.fontWeight == 'bold') {
                                displayInventoryList(button);
                                break;
                            }
                        }
                    }
                }
            });
        });
    });
}

function membersButtonClick() {

    tabButtonClick('membersDiv');

    var membersTable = document.getElementById('membersTable');

    database.ref('Users').once('value', function(snapshot) {

        var str = '<thead><th colspan="4">MEMBERS</th></thead><th>Name</th> <th>Role</th> <th>Contact</th> <th>BITS ID</th>';
        var membersList = snapshot.val();
        var roleText = "";

        for (var key in membersList) {
            switch (membersList[key].role) {
                case 0:
                    roleText = "AVU Unit/Faculty";
                    break;
                case 1:
                    roleText = "Co-ordinator";
                    break;
                case 2:
                    roleText = "Core Member";
                    break;
                case 3:
                    roleText = "Crew Member";
                    break;
                default:
                    roleText = "";

            }
            str += "<tr> <td>" + membersList[key].name + "</td><td>" + roleText +
                "</td><td>" + membersList[key].phone + "</td><td>" + key + "</td></tr>";
        }
        membersTable.innerHTML = str;
    });

}
