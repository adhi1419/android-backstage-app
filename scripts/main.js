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
                        setTimeout(function() {
                            loginText.innerHTML = "";
                        }, 5000);
                    });
            }
        }
    });


    var itemsButton = document.getElementById('itemsButton');
    itemsButton.onclick = itemsButtonClick;
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

function displayModal(displayModalMessage, validateDisplayModalText, callbackFunc) {

    var displayModal = document.getElementById('displayModal');
    var displayModalText = document.getElementById('displayModalText');
    var validateDisplayModalButton = document.getElementById('validateDisplayModalButton');

    displayModalText.innerHTML = displayModalMessage;
    validateDisplayModalButton.innerHTML = validateDisplayModalText;

    var displayModalClose = document.getElementById('displayModalClose');
    // When the user clicks on the button, open the modal
    displayModal.style.display = "block";

    validateDisplayModalButton.onclick = function() {
        displayModal.style.display = "none";
        callbackFunc(true);
    }

    // When the user clicks on <span> (x), close the modal
    displayModalClose.onclick = function() {
            displayModal.style.display = "none";
            callbackFunc(false);
        }
        // When the user clicks anywhere outside of the modal, close it
    window.onclick = function(event) {
        if (event.target == displayModal) {
            displayModal.style.display = "none";
            callbackFunc(false);
        }
    }

}


function tabButtonClick(currTab) {
    var tabs = ["homeDiv", "eventDiv", "membersDiv", "inventoryDiv", "itemsDiv"];
    for (var i = 0; i < tabs.length; i++) {
        if (tabs[i] == currTab) {
            var currentTab = document.getElementById(currTab);
            currentTab.style.display = "block";
        } else {
            var otherTab = document.getElementById(tabs[i]);
            otherTab.style.display = "none";

            var tabClone = otherTab.cloneNode(true);
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
    var homeProfile = document.getElementById('homeProfile');
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
        homeProfile.style.display = "block";
    });

    var changePasswordModal = document.getElementById('changePasswordModal');
    var changePasswordModalContent = changePasswordModal.innerHTML;

    var changePasswordButton = document.getElementById('changePasswordButton');
    changePasswordButton.onclick = function() {

        var currentUser = firebase.auth().currentUser;

        var changePasswordModalClose = document.getElementById('changePasswordModalClose');
        // When the user clicks on the button, open the modal
        changePasswordModal.style.display = "block";

        var validateChangePasswordButton = document.getElementById('validateChangePasswordButton');
        validateChangePasswordButton.onclick = function(eventObj) {

            var changePasswordText = document.getElementById('changePasswordText');
            var newPassword = document.getElementById('newPassword');
            var newPasswordFieldText = document.getElementById('newPasswordFieldText');

            currentUser.updatePassword(newPassword.value).then(function() {
                changePasswordModal.style.display = "none";
                changePasswordModal.innerHTML = changePasswordModalContent;
                displayModal("Update Successful", "K", function callbackFunc(val) {});
            }, function(error) {
                changePasswordModal.style.display = "none";
                changePasswordModal.innerHTML = changePasswordModalContent;
                displayModal("Couldn't Update! Login Again", "K", function callbackFunc(val) {});
            });
        }

        // When the user clicks on <span> (x), close the modal
        changePasswordModalClose.onclick = function() {
                changePasswordModal.style.display = "none";
                changePasswordModal.innerHTML = changePasswordModalContent;
            }
            // When the user clicks anywhere outside of the modal, close it
        window.onclick = function(event) {
            if (event.target == changePasswordModal) {
                changePasswordModal.style.display = "none";
                changePasswordModal.innerHTML = changePasswordModalContent;
            }
        }

    }

}

function eventsButtonClick() {

    tabButtonClick("eventDiv");

    //add event modal comes here
    var addEventModal = document.getElementById('addEventModal');
    var addEventButton = document.getElementById("addEventButton");
    // Get the <span> element that closes the modal
    var addEventModalClose = document.getElementById("addEventModalClose");
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
    addEventModalClose.onclick = function() {
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
        var str = '';
        for (var key in data) {
            if (data.hasOwnProperty(key)) {
                var buttonId = key + 'button';
                //converting firebase data to html
                str = "<tr> <td>" + data[key].name + "</td><td>" + data[key].place + "</td><td>" + data[key].start + "</td><td>" + data[key].end + '</td><td><button class="buttonA" id="' + buttonId + '">X</button></td></tr>' + str;
            }
        }
        str = '<thead><th colspan="4">EVENTS LIST</th></thead><th>Event Name</th> <th> Venue </th> <th> Entry Time </th> <th>Exit Time</th>' + str;
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
                displayModal("You sure? All items corresponding to this event will also be deleted", "Yep", function callbackFunc(val) {
                    if (val) {
                        var button = event.target;
                        var eventKey = button.id.substring(0, button.id.length - 6);
                        database.ref('Event/' + eventKey + '/items').once('value', function(snapshot) {
                            var itemsList = snapshot.val();
                            for (var key in itemsList) {
                                var itemId = itemsList[key].item;
                                database.ref('Items/' + itemId + '/history/' + key).remove();
                                database.ref('Inventory/' + key).remove();
                            }
                        });
                        database.ref('Event/' + eventKey).remove();
                    }
                });
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

    database.ref('Items').once('value', function(snapshot) {
        var itemsList = snapshot.val();

        //issue item modal comes here
        var issueItemModal = document.getElementById('issueItemModal');

        var issueItemModalContent = issueItemModal.innerHTML;
        var issueItemButton = document.getElementById("issueItemButton");
        // Get the <span> element that closes the modal

        issueItemButton.onclick = function() {


            var issueItemModalClose = document.getElementById('issueItemModalClose');
            // When the user clicks on the button, open the modal
            var eventSelect = document.getElementById('eventSelect');
            var eventListRef = database.ref('Event');
            eventListRef.once("value").then(function(snapshot) {
                var eventList = snapshot.val();
                var str = '';
                for (var key in eventList) {
                    str += '<option id="' + key + 'option">' + eventList[key].name + '</option>';
                }
                eventSelect.innerHTML = str;
            });

            issueItemModal.style.display = "block";
            var issueItemModalContentBefore = document.getElementById('issueItemModalContentBefore');

            var issueMoreItemsButton = document.getElementById('issueMoreItemsButton');
            issueMoreItemsButton.onclick = function() {
                var newInputField = document.createElement('div');
                newInputField.innerHTML = '<p style="display:inline-block"></p><input type="number" class="issueItemId"></input>';
                issueItemModalContentBefore.appendChild(newInputField);
            }

            var validateIssueItemButton = document.getElementById('validateIssueItemButton');
            validateIssueItemButton.onclick = function(eventObj) {

                var currentdate = new Date();
                var datetime = currentdate.getFullYear() + "-" + (currentdate.getMonth() + 1) + "-" + currentdate.getDate() + " " +
                    currentdate.getHours() + ":" + currentdate.getMinutes() + ":" + currentdate.getSeconds();
                var selOptionId = eventSelect.options[eventSelect.selectedIndex].id;
                var eventKey = selOptionId.substring(0, selOptionId.length - 6);

                var issueItemId = document.getElementsByClassName('issueItemId');

                var str = '';
                var str2 = '';

                for (var i = 0; i < issueItemId.length; i++) {

                    var itemId = issueItemId[i].value;

                    if (itemsList.hasOwnProperty(itemId)) {
                        issueItem(itemId, issueItemId[issueItemId.length - 1].value);
                    } else {
                        str += itemId + ',';
                    }

                    function issueItem(itemId, finalItemId) {

                        database.ref('Items/' + itemId + '/history').limitToLast(1).once('value', function(snapshot) {

                            var lastKey;
                            for (var key in snapshot.val())
                                lastKey = key;
                            database.ref('Inventory/' + lastKey).once('value', function(snapshot) {
                                var itemDetails = snapshot.val();

                                if ((itemDetails != null) && (itemDetails.status == "0")) {
                                    notReturnedItems(itemId);
                                } else {
                                    var itemEntry = {
                                        item: itemId
                                    };
                                    var newKey = database.ref('Inventory').push().key;
                                    database.ref('Event/' + eventKey + '/items/' + newKey).set(itemEntry);
                                    itemEntry = {
                                        event: eventKey
                                    };
                                    database.ref('Items/' + itemId + '/history/' + newKey).set(itemEntry);
                                    itemEntry = {
                                        event: eventKey,
                                        id: currentUserId,
                                        status: "0",
                                        time: datetime,
                                        item: itemId
                                    };
                                    database.ref('Inventory/' + newKey).set(itemEntry);
                                }

                                if (itemId == finalItemId && str2 != '')
                                    setTimeout(
                                        displayModal(str2.substring(0, str2.length - 1) + " : Item(s) already issued", "K", function callbackFunc(val) {}),
                                        5000);

                            });
                        });

                    }

                    function notReturnedItems(itemId) {
                        str2 += itemId + ',';
                    }

                }
                issueItemModal.style.display = "none";
                issueItemModal.innerHTML = issueItemModalContent;

                if (str != '') {
                    displayModal(str.substring(0, str.length - 1) + " : Item(s) not registered in database", "K", function callbackFunc(val) {});
                }
            }

            // When the user clicks on <span> (x), close the modal
            issueItemModalClose.onclick = function() {
                    issueItemModal.style.display = "none";
                    issueItemModal.innerHTML = issueItemModalContent;
                }
                // When the user clicks anywhere outside of the modal, close it
            window.onclick = function(event) {
                if (event.target == issueItemModal) {
                    issueItemModal.style.display = "none";
                    issueItemModal.innerHTML = issueItemModalContent;
                }
            }

        }

        //return item modal comes here
        var returnItemModal = document.getElementById('returnItemModal');
        var returnItemModalContent = returnItemModal.innerHTML;
        var returnItemButton = document.getElementById("returnItemButton");

        // When the user clicks on the button, open the modal
        returnItemButton.onclick = function() {

            // Get the <span> element that closes the modal
            var returnItemModalClose = document.getElementById('returnItemModalClose')

            returnItemModal.style.display = "block";

            var returnItemModalContentBefore = document.getElementById('returnItemModalContentBefore');

            var returnMoreItemsButton = document.getElementById('returnMoreItemsButton');
            returnMoreItemsButton.onclick = function() {
                var newInputField = document.createElement('div');
                newInputField.innerHTML = '<p style="display:inline-block"></p><input type="number" class="returnItemId"></input>';
                returnItemModalContentBefore.appendChild(newInputField);
            }

            var validateReturnItemButton = document.getElementById('validateReturnItemButton');
            validateReturnItemButton.onclick = function(eventObj) {

                    var returnItemId = document.getElementsByClassName('returnItemId');

                    var str = '';
                    var str2 = '';

                    for (var i = 0; i < returnItemId.length; i++) {

                        var itemId = returnItemId[i].value;

                        if (itemsList.hasOwnProperty(itemId)) {
                            returnItem(itemId, returnItemId[returnItemId.length - 1].value);
                        } else {
                            str += itemId + ',';
                        }

                        function returnItem(itemId, finalItemId) {

                            database.ref('Items/' + itemId + '/history').limitToLast(1).once('value', function(snapshot) {

                                var lastKey;
                                for (var key in snapshot.val())
                                    lastKey = key;

                                database.ref('Inventory/' + lastKey).once('value', function(snapshot) {

                                    var itemDetails = snapshot.val();

                                    if ((itemDetails == null) || (itemDetails.status != "0")) {
                                        notIssuedItems(itemId);
                                    } else {
                                        var currentdate = new Date();
                                        var datetime = currentdate.getFullYear() + "-" + (currentdate.getMonth() + 1) + "-" + currentdate.getDate() + " " +
                                            currentdate.getHours() + ":" + currentdate.getMinutes() + ":" + currentdate.getSeconds();
                                        itemDetails.status = datetime;
                                        database.ref('Inventory/' + key).update(itemDetails);
                                    }

                                    if (itemId == finalItemId && str2 != '')
                                        setTimeout(
                                            displayModal(str2.substring(0, str2.length - 1) + " : Item(s) are not issued", "K", function callbackFunc(val) {}),
                                            5000);

                                });
                            });

                        }

                        function notIssuedItems(itemId) {
                            str2 += itemId + ',';
                        }

                    }

                    returnItemModal.style.display = "none";
                    returnItemModal.innerHTML = returnItemModalContent;

                    if (str != '') {
                        displayModal(str.substring(0, str.length - 1) + " : Item(s) not registered in database", "K", function callbackFunc(val) {});
                    }
                }
                // When the user clicks on <span> (x), close the modal
            returnItemModalClose.onclick = function() {
                    returnItemModal.style.display = "none";
                    returnItemModal.innerHTML = returnItemModalContent;
                }
                // When the user clicks anywhere outside of the modal, close it
            window.onclick = function(event) {
                if (event.target == returnItemModal) {
                    returnItemModal.style.display = "none";
                    returnItemModal.innerHTML = returnItemModalContent;
                }
            }

        }
    });


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
                var pageNosDiv = document.getElementById('pageNosDiv');

                function fullInventoryListButtonClick(eventObj) {
                    eventInventoryList.style.display = "none";
                    pageNosDiv.style.display = "none";
                    displayInventoryList(eventObj.target, 1);
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
                    pageNosDiv.style.display = "none";

                    for (var key in eventList) {
                        var buttonId = '' + key + 'showButton';
                        var button = document.getElementById(buttonId);

                        button.onclick = function(eventObj) {
                            for (var key in eventList) {
                                var buttonId = '' + key + 'showButton';
                                if (eventObj.target.id == buttonId) {
                                    eventObj.target.style.fontWeight = 'bold';
                                    eventObj.target.style.color = '#B46D6A';
                                } else {
                                    var button = document.getElementById(buttonId);
                                    button.style.fontWeight = 'normal';
                                    button.style.color = '#FFFFFF';
                                }
                            }
                            displayInventoryList(eventObj.target, 1);
                        };

                    }

                }

                function displayInventoryList(eventObjTarget, currPageNo) {

                    var str = '';
                    var str2 = '<p style="display:inline-block"> Page(s):  </p>';
                    var displayList = {};
                    var tempDisplayList = {};
                    var pageNos;
                    var itemsToSkip;
                    var ct = 1;

                    if (eventObjTarget.id == "fullInventoryListButton") {
                        tempDisplayList = inventoryList;
                    } else {
                        eventKey = eventObjTarget.id.substring(0, eventObjTarget.id.length - 10);
                        for (var key in eventList[eventKey].items) {
                            tempDisplayList[key] = inventoryList[key];
                        }
                    }

                    pageNos = Math.ceil(Object.keys(tempDisplayList).length / 10);
                    itemsToSkip = Object.keys(tempDisplayList).length - (currPageNo * 10);

                    for (var key in tempDisplayList) {

                        if (ct <= itemsToSkip) {
                            //do nothing
                        } else if (ct > itemsToSkip + 10) {
                            break;
                        } else {
                            displayList[key] = tempDisplayList[key];
                        }
                        ct++;
                    }

                    for (var key in displayList) {

                        if (displayList.hasOwnProperty(key)) {
                            //converting firebase data to html
                            var buttonId = '' + key + 'button';
                            var userName = usersList[displayList[key].id].name;
                            var eventKey = displayList[key].event;
                            var buttonText = "";
                            var returnText = "";

                            if (displayList[key].status == "0") {
                                buttonText = "Return";
                                returnText = "Not Returned.\n Expected by " + eventList[eventKey].end;
                            } else {
                                buttonText = "x";
                                returnText = "Returned at " + displayList[key].status;
                            }

                            str = "<tr> <td>" + displayList[key].item + "</td><td>" + eventList[eventKey].name + "," + eventList[eventKey].place +
                                "</td><td>" + userName + "</td><td>" + displayList[key].time + "</td><td>" + returnText +
                                '</td><td><button class="buttonA" id="' + buttonId + '">' + buttonText + '</button></td></tr>' + str;

                        }
                    }
                    str = '<thead><th colspan="5"> INVENTORY LIST </th></thead><th>Item Number</th> <th>Event</th> <th>Issued To</th> <th>Issue Time</th> <th>Return Time</th>' +
                        str;
                    if (Object.keys(displayList).length === 0) {
                        str = '<thead><th colspan="5"> NO ITEMS IN EVENT </th></thead>';
                    }


                    inventoryListTable.innerHTML = str;
                    inventoryListTable.style.display = "table";

                    if (pageNos >= 2) {

                        for (var i = 1; i <= pageNos; i++) {
                            str2 += '<button id="pageNo' + i + '" class="buttonA">' + i + '</button>';
                        }

                        str2 += '<hr>'
                        pageNosDiv.innerHTML = str2;

                        var currPageButton = document.getElementById('pageNo' + currPageNo);
                        currPageButton.style.fontWeight = "bold";
                        currPageButton.style.color = "#B46D6A";

                        //setting listeners for page numbers
                        for (var i = 1; i <= pageNos; i++) {

                            var buttonId = 'pageNo' + i;
                            var button = document.getElementById(buttonId);
                            button.onclick = function(eventObj) {

                                var eventObjId = eventObj.target.id;
                                var pageId = parseInt(eventObjId.substring(eventObjId.length - 1), 10);
                                var fullInventoryListButton = document.getElementById('fullInventoryListButton');

                                if (eventInventoryList.style.display == "none") {
                                    displayInventoryList(fullInventoryListButton, pageId);
                                } else {
                                    for (var key in eventList) {
                                        var buttonId = '' + key + 'showButton';
                                        var button = document.getElementById(buttonId);
                                        if (button != null) {
                                            if (button.style.fontWeight == 'bold') {
                                                displayInventoryList(button, pageId);
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        pageNosDiv.style.display = "inline-block";

                    } else {
                        pageNosDiv.style.display = "none";
                    }

                    //setting listeners for delete/return event button
                    for (var key in displayList) {

                        var buttonId = '' + key + 'button';
                        var button = document.getElementById(buttonId);
                        button.onclick = deleteOrReturnItemClick;

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
                    displayInventoryList(fullInventoryListButton, 1);
                } else {
                    for (var key in eventList) {
                        var buttonId = '' + key + 'showButton';
                        var button = document.getElementById(buttonId);
                        if (button != null) {
                            if (button.style.fontWeight == 'bold') {
                                displayInventoryList(button, 1);
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

function itemsButtonClick() {

    tabButtonClick('itemsDiv');

    //add item modal comes here
    var addItemModal = document.getElementById('addItemModal');
    var addItemModalContent = addItemModal.innerHTML;
    var addItemButton = document.getElementById('addItemButton');

    addItemButton.onclick = function() {


        var addItemModalClose = document.getElementById('addItemModalClose');
        // When the user clicks on the button, open the modal

        var itemTypeSelect = document.getElementById('itemTypeSelect');
        var itemTypeList = ["XLR", "5 to 5", "M to 5", "F to 5", "5 to 3.5", "Speak-On", "Speaker", "Amp"];
        var str = '';
        for (var key in itemTypeList) {
            str += '<option>' + itemTypeList[key] + '</option>';
        }
        itemTypeSelect.innerHTML = str;

        addItemModal.style.display = "block";

        var validateAddItemButton = document.getElementById('validateAddItemButton');
        validateAddItemButton.onclick = function(eventObj) {

            var selOption = itemTypeSelect.options[itemTypeSelect.selectedIndex].innerHTML;
            var addItemId = document.getElementById('addItemId');
            var addItemAttributes = document.getElementById('addItemAttributes');

            var itemEntry = {
                type: selOption,
                details: addItemAttributes.value
            };
            database.ref('Items/' + addItemId.value).set(itemEntry);
            addItemModal.style.display = "none";
            addItemModal.innerHTML = addItemModalContent;
        }

        // When the user clicks on <span> (x), close the modal
        addItemModalClose.onclick = function() {
                addItemModal.style.display = "none";
                addItemModal.innerHTML = addItemModalContent;
            }
            // When the user clicks anywhere outside of the modal, close it
        window.onclick = function(event) {
            if (event.target == addItemModal) {
                addItemModal.style.display = "none";
                addItemModal.innerHTML = addItemModalContent;
            }
        }

    }

    //delete item modal comes here
    var deleteItemModal = document.getElementById('deleteItemModal');
    var deleteItemModalContent = deleteItemModal.innerHTML;
    var deleteItemButton = document.getElementById('deleteItemButton');

    deleteItemButton.onclick = function() {


        var deleteItemModalClose = document.getElementById('deleteItemModalClose');
        // When the user clicks on the button, open the modal

        deleteItemModal.style.display = "block";

        var validateDeleteItemButton = document.getElementById('validateDeleteItemButton');
        validateDeleteItemButton.onclick = function(eventObj) {

            var deleteItemId = document.getElementById('deleteItemId');

            database.ref('Items/' + deleteItemId.value).remove();
            deleteItemModal.style.display = "none";
            deleteItemModal.innerHTML = deleteItemModalContent;
        }

        // When the user clicks on <span> (x), close the modal
        deleteItemModalClose.onclick = function() {
                deleteItemModal.style.display = "none";
                deleteItemModal.innerHTML = deleteItemModalContent;
            }
            // When the user clicks anywhere outside of the modal, close it
        window.onclick = function(event) {
            if (event.target == deleteItemModal) {
                deleteItemModal.style.display = "none";
                deleteItemModal.innerHTML = deleteItemModalContent;
            }
        }

    }



}
