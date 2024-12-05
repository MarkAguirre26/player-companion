let platform = null;
let playedShoe = 0;
let dateCreated;
var freezeState = 'N';
var totalProfit = 0;
var stompClient = null;

var sensitivityGlobal = 50;


// Create an Audio object with the MP3 file
// const alertSound = new Audio('sounds/reverby-notification-sound-246407.mp3');


// Create an Audio object for the notification sound
const notificationSound = new Audio('sounds/reverby-notification-sound-246407.mp3');

// Attempt to play the sound immediately
notificationSound.play().catch(error => {
    console.log('Autoplay blocked. Waiting for user interaction.');

    // Add an event listener to play sound once on first user interaction
    document.addEventListener('click', playNotificationSound, {once: true});
    document.addEventListener('mousemove', playNotificationSound, {once: true});
});

// Function to play sound and remove listeners after it's triggered
function playNotificationSound() {
    notificationSound.play();
    document.removeEventListener('click', playNotificationSound);
    document.removeEventListener('mousemove', playNotificationSound);
}


const ctx = document.getElementById('profitsGraph').getContext('2d');
let dummyGraph; // Variable to hold the chart instance


function toggleMenu() {

    const menu = document.getElementById('dropdownMenu');
    menu.classList.toggle('hidden');
}


function showNotification(message) {
    const notification = document.getElementById('notification');
    notification.style.display = 'block';

    $('#message').text(message);
    // Hide the notification after 1 second
    setTimeout(() => {
        notification.style.display = 'none';
    }, 4000);
}


function connect() {
    try {
        var socket = new SockJS('/ws');
        stompClient = Stomp.over(socket);
        stompClient.connect({}, function (frame) {
            console.log('Connected: ' + frame);
            stompClient.subscribe('/topic/messages', function (message) {
                // showMessage(message.body);
                var messgae = message.body;

                // console.log(message);

                if (messgae.includes('SicBo:')) {


                    const pauseButton = document.getElementById('pauseButton');
                    const playButton = document.getElementById('playButton');

                    // Check if pauseButton is hidden
                    const isPauseButtonHidden = window.getComputedStyle(pauseButton).display === 'none' ||
                        window.getComputedStyle(pauseButton).visibility === 'hidden';


                    // Check if playButton is hidden
                    const isPlayButtonHidden = window.getComputedStyle(playButton).display === 'none' ||
                        window.getComputedStyle(playButton).visibility === 'hidden';


                    // var pauseAndPlayValue = localStorage.getItem('pauseAndPlay'); // Corrected key

                    if (isPlayButtonHidden) {

                        const dice = messgae.split(":");
                        var dice_size = dice[1];
                        var diceSum = dice[2];
                        localStorage.removeItem('diceSum');
                        localStorage.setItem('diceSum', diceSum);
                        showSpinner();
                        playGame(dice_size + ":" + diceSum);
                    }


                }


            });
        }, function (error) {
            console.error('Error: ' + error);
        });
    } catch (e) {
        console.log(e);
    }

}


function showSpinner() {
    const circleContainer = document.getElementById("circleContainer");
    const spinnerOverlay = document.getElementById("spinnerOverlay");

    // Get the position and dimensions of circleContainer
    const rect = circleContainer.getBoundingClientRect();

    // Show the spinner overlay
    spinnerOverlay.style.display = "flex";
    spinnerOverlay.style.position = "fixed"; // Change to fixed for screen-wide overlay
    spinnerOverlay.style.top = "0"; // Cover the whole screen
    spinnerOverlay.style.left = "0"; // Cover the whole screen
    spinnerOverlay.style.width = "100vw"; // Full viewport width
    spinnerOverlay.style.height = "100vh"; // Full viewport height
    spinnerOverlay.style.zIndex = "9999"; // Ensure it is above everything, including modals
}


function hideSpinner() {
    // Hide the overlay and spinner
    document.getElementById("spinnerOverlay").style.display = "none";
}


async function getPlatform() {
    try {
        const response = await fetch('/api/game/platform', {
            method: 'GET',
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const r = await response.text(); // Assuming the response is a string

        localStorage.setItem("platform", r);

    } catch (error) {
        alert('An error occurred: ' + error.message);
    }
}


$(document).ready(function () {

    // const smallButton = document.getElementById("smallButton");
    // const bigButton = document.getElementById("bigButton");


    (async () => {
        await getPlatform();
        await getJournal('today');
        await currentState();


    })();

    getPauseAndPlay();
    getFreezeStateAsync();

    // Get modal and buttons
    const modal = document.getElementById('yesNoModal');
    const openModalButton = document.getElementById('openModalButton');
    const yesButton = document.getElementById('yesButton');
    const noButton = document.getElementById('noButton');
    const cancelButton = document.getElementById('cancelButton');

    const pauseButton = document.getElementById('pauseButton');
    const playButton = document.getElementById('playButton');

    const backButton = document.getElementById('backButton');

    platform = localStorage.getItem("platform");

    console.log(platform);

    if (platform === null) {
        location.reload();
    }
    // Check the current page's URL
    if (platform === "SicBo") {

        var pauseAndPlayValue = localStorage.getItem('pauseAndPlay'); // Corrected key

        if (pauseAndPlayValue === 'play') {
            pauseButton.style.display = 'block';
            playButton.style.display = 'none';
        } else {
            pauseButton.style.display = 'none';
            playButton.style.display = 'block';
        }

        $("#buttons-collection").addClass("hidden");


    } else {

        pauseButton.style.display = 'none'; // This will hide the element
        playButton.style.display = 'none';
        $("#buttons-collection").removeClass("hidden");
    }


    backButton.addEventListener('click', () => {
        undoHand();

    });

    pauseButton.addEventListener('click', () => {
        setPauseAndPlay('pause');

    });

    playButton.addEventListener('click', () => {
        setPauseAndPlay('play');


    });


    // Close modal and handle Yes/No actions
    cancelButton.addEventListener('click', () => {
        modal.classList.add('hidden');

    });


    // Close modal and handle Yes/No actions
    noButton.addEventListener('click', () => {
        processEventToReset("no");
    });


    yesButton.addEventListener('click', async () => {
        processEventToReset("yes");
    });


    // Event listener for PLAYER button
    $('#smallButton').on('click', function () {
        showSpinner();
        playGame("s:4");

    });

    // Event listener for BANKER button
    $('#bigButton').on('click', function () {
        showSpinner();
        playGame("b:11");
    });

    $('#trippleButton').on('click', function () {
        showSpinner();
        playGame("t:15");
    });


    // Toggle menu visibility when clicking the button
    document.getElementById('threeDotsButton').addEventListener('click', function (event) {
        event.stopPropagation(); // Prevent click from propagating to document listener
        toggleMenu();
    });

    // Close menu when clicking outside
    document.addEventListener('click', function (event) {
        const menu = document.getElementById('dropdownMenu');
        const button = document.getElementById('threeDotsButton');

        // Check if the menu is open and the click is outside the menu and button
        if (!menu.classList.contains('hidden') && !menu.contains(event.target) && event.target !== button) {
            menu.classList.add('hidden');
        }
    });


    // Event listener for RESET GAME button
    $('#resetButton').on('click', async function () {

        let message = $('#status').text();
        if (message.includes("Archived")) {
            return;
        }
        modal.classList.remove('hidden');

    });


    getFreezeStateAsync();


    if (platform === "SicBo") {

        connect();
    }


    postGameParameters();


    const slider = document.getElementById('Sensitivity');
    const output = document.getElementById('SensitivityValue');

    // Update the displayed value whenever the slider changes
    slider.oninput = function () {
        output.textContent = "Prediction Sensitivity(" + this.value + ")";
    }


    document.getElementById('reverseBet').addEventListener('change', function () {
        const isChecked = this.checked;

        localStorage.setItem('reverseBetState', isChecked);


    });


    document.getElementById('hideHandResult').addEventListener('change', function () {
        const isChecked = this.checked;

        localStorage.setItem('hideHandResultState', isChecked);
        setHideHandResult();
    });


    const savedState = localStorage.getItem('reverseBetState');

    if (savedState === 'true') {
        $('#reverseBet').prop('checked', true);
    } else {
        $('#reverseBet').prop('checked', false);
    }

    // --------------------------------------------------


    setHideHandResult();


});

function setHideHandResult() {

    const hideHandResultSavedState = localStorage.getItem('hideHandResultState');

    if (hideHandResultSavedState === 'true') {
        $('#hideHandResult').prop('checked', true);
        $('#handResultContainer').css('display', 'none'); // Use jQuery's .css() method
    } else {
        $('#hideHandResult').prop('checked', false);
        $('#handResultContainer').css('display', 'flex');
    }
}

function undoHand() {
    $.ajax({
        url: '/api/sicBo/undo', // Change to your API endpoint
        type: 'POST',
        contentType: 'application/json', // Specify content type
        success: function (response) {
            getPauseAndPlay();
            getFreezeStateAsync();

            (async () => {

                await getJournal('today');
                await currentState();

            })();

        },
        error: function (xhr, status, error) {
            alert('An error occurred: ' + xhr.responseText);
        }
    });
}


function postGameParameters() {
    $('.bg-blue-500').click(function (event) {
        event.preventDefault(); // Prevent the default form submission

        const slider = document.getElementById('Sensitivity');

        const isCheckedOpenAi = document.getElementById('openAi').checked
            ? 'OPEN_AI'
            : document.getElementById('patternAdaptive').checked
                ? 'PATTERN'
                : 'RECOGNIZER';


        // Collect input values from the form
        const data = {
            stopLoss: $('#stopLoss').val(),
            stopProfit: $('#stopProfit').val(),
            trailingStopSpread: $('#trailingStopSpread').val(),
            trailingStopActivation: $('#trailingStopActivation').val(),
            moneyManagement: $('#moneyManagement').val(),
            strategy: isCheckedOpenAi,
            isShield: $('#isShield').is(':checked') ? 1 : 0, // Convert checkbox to integer
            sensitivity: slider.value,
            isCompounding: $('#isCompounding').is(':checked') ? 1 : 0, // Convert checkbox to integer
            stopTrigger: $('#stopTrigger').val(),
            virtualWin: $('#virtualWin').val(),
            startingFund: $('#startingFund').val(),
            dailyGoalPercentage: $('#dailyGoalPercentage').val(),
            betAmount: $('#betAmount').val()
        };

        // Send AJAX POST request
        $.ajax({
            url: '/api/game/parameters', // Change to your API endpoint
            type: 'POST',
            contentType: 'application/json', // Specify content type
            data: JSON.stringify(data), // Convert data to JSON
            success: function (response) {
                // alert('Parameters saved successfully!');
                // Optionally, you can clear the input fields or perform other actions here
                fetchGameParameters();

                // Show success message
                $('#successMessage').text("Game parameters saved successfully!").removeClass('hidden');

                // Hide the success message after 2 seconds
                setTimeout(() => {
                    $('#successMessage').addClass('hidden'); // Add hidden class to hide the message
                }, 2000);


            },
            error: function (xhr, status, error) {
                alert('An error occurred: ' + xhr.responseText);
            }
        });
    });
}

async function fetchGameParameters() {
    try {
        const response = await fetch('/api/game/parameters', {
            method: 'GET', // HTTP method
            headers: {
                'Content-Type': 'application/json', // Set the content type to JSON
            },
        });

        if (!response.ok) {
            throw new Error(`HTTP error! Status: ${response.status}`);
        }

        if (!response.ok) {
            throw new Error(`HTTP error! Status: ${response.status}`);
        }

        const data = await response.json(); // Parse the JSON response

        // Populate input fields with retrieved data
        $('#stopLoss').val(data.stopLoss);
        $('#stopProfit').val(data.stopProfit);
        $('#trailingStopSpread').val(data.trailingStopSpread);
        $('#trailingStopActivation').val(data.trailingStopActivation);
        $('#moneyManagement').val(data.moneyManagement);

        if (data.strategy === 'OPEN_AI') {
            $('#openAi').prop('checked', true);
        } else if (data.strategy === 'PATTERN') {
            $('#patternAdaptive').prop('checked', true);
        } else {
            $('#recognizer').prop('checked', true);
        }

        $('#isShield').prop('checked', data.isShield === 1);
        $('#isCompounding').prop('checked', data.isCompounding === 1);
        $('#stopTrigger').val(data.stopTrigger);
        $('#virtualWin').val(data.virtualWin);
        $('#startingFund').val(data.startingFund);
        $('#currentFund').val(data.currentFund);
        $('#dailyGoalPercentage').val(data.dailyGoalPercentage);
        $('#betAmount').val(data.betAmount);
        $('#Sensitivity').val(data.sensitivity);
        $('#dailyGoalValue').val(Math.round((data.dailyGoalPercentage / 100) * data.startingFund / 10) * 10);


        $("#strategyLabel").text(toCamelCase(data.moneyManagement.replace("_", " ")));

        const SensitivityValue = document.getElementById('SensitivityValue');
        SensitivityValue.textContent = "Prediction Sensitivity(" + data.sensitivity + ")";

        sensitivityGlobal = data.sensitivity;


    } catch (error) {
        console.error("Error fetching game parameters:", error);
        // alert("Failed to fetch game parameters. Please try again.");
    }
}


async function processEventToReset(answer) {


    const modal = document.getElementById('yesNoModal');
    showSpinner();
    try {
        const response = await $.post('/api/sicBo/reset', {yesNo: answer});
        displayResultToUi(response);
    } catch (error) {
        $('#resetButton').prop('disabled', false);
        $('#status').text(`Error: ${error.responseText || 'Unexpected error'}`);
    } finally {
        hideSpinner();
        $('#undoButton').hide();
        modal.classList.add('hidden');
    }

}


async function playGame(userInput) {

    try {


        var userBetValue = "";
        const savedState = localStorage.getItem('reverseBetState');

        if (savedState === 'true') {
            userBetValue = $('#recommendedBet').text() === 'Small' ? 'Big' : 'Big' ? 'Small' : '-';
        } else {
            userBetValue = $('#recommendedBet').text();
        }

        // console.log(userBetValue);


        const response = await $.post('/api/sicBo/play', $.param({
            userInput,
            recommendedBet: userBetValue,
            suggestedUnit: $('#suggestedBetUnit').text()
        }));

        // console.log(response);


        displayResultToUi(response);
    } catch (xhr) {

        $('#status').text(`Error: ${xhr.responseText || 'Unexpected error'}`);
    }
}


async function displayResultToUi(response) {
    // console.log(response);

    hideSpinner();

    const savedState = localStorage.getItem('reverseBetState');

    const {handCount, wins, losses, profit} = response.gameStatus;
    const statusMessage = response.message;
    // const tempMessage = (statusMessage.includes('Wait') && statusMessage.includes('Wait for virtual win')) ? "" : statusMessage;

    const isRecognizerChecked = document.getElementById('recognizer').checked


    if (isRecognizerChecked) {
        const reverseBetCheckBox = $('#reverseBet');
        if (losses > wins) {
            reverseBetCheckBox.prop('checked', true);
        } else {
            reverseBetCheckBox.prop('checked', false);
        }
    }


    const statusElement = $('#status');
    // Handle status, reset, and undo button visibility
    statusElement.toggle(handCount > 0 || statusMessage.includes("Daily limit!"));

    statusElement
        .text(statusMessage)
        .css({
            'color': statusMessage.includes("profit") ? 'green' : statusMessage.includes("loss") ? 'red' : 'black',
            'display': 'none' // Add this line to hide the element
        });

    if (statusMessage.includes("reset") || statusMessage.includes("Initialized")) {
        $("#handResultColumn").empty();
        statusElement.text("");
        await getJournal("today");
    }

    $("#baseBetUnit").val(response.baseBetUnit);

    $("#initialPlayingUnit").val(response.initialPlayingUnits);

    // Display betting results
    $('#bettingResults').html(`
                <div class="bg-white shadow-md rounded-lg overflow-hidden">



                <div class="bg-gray-200 text-gray-800 text-xs font-semibold p-1 flex justify-between">
                    <span id="playedShoe">Round: ${playedShoe}</span>

                   
                    
                    <span id="handCount" >Rolls: ${handCount}</span>
                </div>



                <div class="p-2 space-y-1">
                    <div class="flex justify-between border-b pb-1">
                    <span class="font-bold text-xs">Wins/Losses:</span><span class="text-xs">${wins}/${losses}</span>
                    </div>
                  
                    <div class="flex justify-between border-b pb-1" style="${!response.trailingStop ? 'display:none;' : ''}">
                    <span class="font-bold text-xs">Trailing Stop:</span><span class="text-xs">${parseInt(response.trailingStop) || 0}</span>
                    </div>

                    <div class="flex justify-between border-b pb-1">
                    <span class="font-bold text-xs">Net Profit:</span>
                    <span class="text-xs ${profit >= 0 ? 'text-green-600' : 'text-red-600'}">${profit.toFixed(0)}</span>
                    </div>
                    <div class="flex justify-between border-b pb-1">
                    <span class="font-bold text-xs">Stake:</span><span id="nextbetText" class="text-sm font-bold">${response.suggestedBetUnit.toFixed(0)}</span>
                    </div>
                    <div class="flex justify-between">
                    <span class="font-bold text-xs">Next Bet:</span>
                    
                  <span id="nextbet-display" style="width: 45px;" class="${savedState === 'true' ?
        (statusMessage === 'Wait for virtual win.' ? 'text-sm' :
            response.recommendedBet.includes('Wait') ? 'text-sm' :
                response.recommendedBet === 'Small' ? 'h-5 bg-red-500 text-sm text-white flex items-center justify-center rounded shadow-lg' :
                    response.recommendedBet === 'Big' ? 'h-5 bg-blue-500 text-sm text-white flex items-center justify-center rounded shadow-lg' :
                        'w-20 h-5 bg-green-500 text-sm text-white flex items-center justify-center rounded shadow-lg') :
        (statusMessage === 'Wait for virtual win.' ? 'text-sm' :
            response.recommendedBet.includes('Wait') ? 'text-sm' :
                response.recommendedBet === 'Small' ? 'h-5 bg-blue-500 text-sm text-white flex items-center justify-center rounded shadow-lg' :
                    response.recommendedBet === 'Big' ? 'h-5 bg-red-500 text-sm text-white flex items-center justify-center rounded shadow-lg' :
                        'w-20 h-5 bg-green-500 text-sm text-white flex items-center justify-center rounded shadow-lg')}">
                    ${savedState === 'true' ?
        (statusMessage === 'Wait for virtual win.' ? 'Wait..' :
            statusMessage.includes('Wait') ? 'Wait..' :
                response.recommendedBet === 'Small' ? 'Big' : 'Small') :
        (statusMessage === 'Wait for virtual win.' ? 'Wait..' :
            statusMessage.includes('Wait') ? 'Wait..' :
                response.recommendedBet)}
                    </span>


                    </div>
                


                </div>
                </div>
                <p id="recommendedBet" style="display:none;">${response.recommendedBet}</p>
                <p id="suggestedBetUnit" style="display:none;">${response.suggestedBetUnit.toFixed(0)}</p>`);


    if (statusMessage.includes("Restart")) {


        $('#infoHeader').text("Game Over");
        $('#infoContent').text(statusMessage);

        // this is to make automated
        setPauseAndPlay('play');
        processEventToReset("yes");


    } else {

        // Update header and content based on total profit
        const headerText = totalProfit > 10 ? "Lucky Day" : "Reminder";
        const contentText = totalProfit > 10
            ? "You're now in profit! Time to call it a day and enjoy your success."
            : "Follow the app's guidelines to protect your bankroll.";

        // Update the UI elements for the header and content
        $('#infoHeader').text(headerText);
        $('#infoContent').text(contentText);

        // Get values from input fields
        const dailyGoalValue = parseFloat($('#dailyGoalValue').val()) || 0;
        const betAmountValue = parseFloat($('#betAmount').val()) || 1; // Default to 1 to prevent division by zero

        // Calculate the unit goal
        const unitGoal = dailyGoalValue / betAmountValue;

        // Check if the total profit has reached or exceeded the daily goal
        if (totalProfit >= unitGoal) {
            $('#infoHeader').text('Daily Goal');
            $('#infoContent').text('Congrats! You successfully hit your daily goal.');
            setPauseAndPlay('play');
        }

    }


    try {

        displayResults(response.sequence.toUpperCase(), response.diceNumber);


    } catch (e) {

    }


    getHandResult(response);
    getActiveUsers();
    // Handle button visibility after game result
    if (statusMessage.includes("reached")) {

        await getJournal("today");
    }


    showorHideStrategiesDots();

    updateProgressBar(response.confidence);
    getFreezeStateAsync();

    // var unitValue = parseFloat($('#nextbetText').text());
    // console.log(unitValue);


    var betUnitValue = getNumbersFromString($("#nextbetText").text());

    if (betUnitValue > 0) {

        betAmountValue = $('#betAmount').val();

        playNotificationSound();


    }


    // // Get the content of the element with ID 'handCount'
    //  const handCountText = $('#handCount').text();
    //  const msgHeader  = $('#infoHeader').text();

    // if (getNumbersFromString(handCountText) >= 11 && !msgHeader.includes("Archive")) {
    //      processEventToReset('yes');
    //      setPauseAndPlay('pause');
    //  }
    if (platform === "Baccarat") {

        const spanElement = document.getElementById('nextbet-display');
        const text = spanElement.textContent; // Get the text content of the span
        spanElement.textContent = text.includes("Small") ? "Player" : text.includes("Big") ? "Banker" : text;


        const handCount = $('#handCount');
        const handCountText = handCount.text();
        handCount.text(handCountText.replace("Rolls", "Hands"))

        const playedShoe = $('#playedShoe');
        const playedShoeText = playedShoe.text();
        playedShoe.text(playedShoeText.replace("Round", "Shoe"))


    }


}


function addContainer(group) {
    const container = document.createElement('div');
    container.classList.add('flex', 'flex-col', 'items-start', 'justify-start', 'h-full');


    // Create circles for each character in the group
    group.split('').forEach((char, index) => {

        const circle = document.createElement('div');
        circle.classList.add('flex', 'items-center', 'justify-center', 'w-6', 'h-6', 'border', 'rounded-full',
            char === 'S' ? 'bg-blue-500' : char === 'B' ? 'bg-red-500' : 'bg-green-500');

        // Create a smaller white circle inside
        const innerCircle = document.createElement('div');
        innerCircle.classList.add('flex', 'items-center', 'justify-center', 'w-4', 'h-4', 'bg-white', 'rounded-full', 'text-xs', 'text-gray-400');
        innerCircle.textContent = '' // Set dice sum for each character

        // Add the inner circle to the outer circle
        circle.appendChild(innerCircle);
        container.appendChild(circle);
    });

    return container;
}

function displayResults(playerAndBankerCollections, diceSumCollection) {
    const results = playerAndBankerCollections.replace("1111", "").split('');
    const diceSums = diceSumCollection.replace("1111", "").split(',');

    // console.log("diceSums"+diceSums);

    // Clear previous results
    document.getElementById('handResultColumn').innerHTML = '';
    document.getElementById('handResultContainer').innerHTML = '';

    let currentChar = results[0];


    let group = currentChar;

    const column = document.getElementById('handResultColumn');
    const circleContainer = document.getElementById('circleContainer');

    for (let i = 1; i < results.length; i++) {
        if (results[i] === currentChar) {
            group += results[i];

        } else {
            column.appendChild(addContainer(group));
            currentChar = results[i];

            group = currentChar;
        }
    }


    // Append the last group after the loop
    column.appendChild(addContainer(group));


    // Auto-scroll the circleContainer to the right after adding new content
    setTimeout(() => {
        circleContainer.scrollLeft = circleContainer.scrollWidth;
    }, 0);


    const innerElements = column.getElementsByClassName('bg-white');


    for (let i = 0; i < innerElements.length + 1; i++) {


        // innerElements.style.display = 'none'; // This will hide the element


        if (platform === "SicBo") {
            innerElements[i].textContent = diceSums[i + 1];
        }

    }


}

function getPauseAndPlay() {

    if (platform === "SicBo") {


        const pauseButton = document.getElementById('pauseButton');
        const playButton = document.getElementById('playButton');

        // Ensure both elements exist
        if (pauseButton && playButton) {

            var pauseAndPlayValue = localStorage.getItem('pauseAndPlay'); // Corrected key

            if (pauseAndPlayValue === 'play') {
                pauseButton.style.display = 'block';
                playButton.style.display = 'none';
            } else {
                pauseButton.style.display = 'none';
                playButton.style.display = 'block';
            }

            showorHideStrategiesDots();
        } else {
            console.log('Pause or Play button not found.');
        }
    }

}


function setPauseAndPlay(pauseAndPlayValue) {

    localStorage.setItem('pauseAndPlay', pauseAndPlayValue);
    getPauseAndPlay();
}


// async function getStrategy() {


//     try {
//         const response = await $.ajax({
//             url: '/api/sicBo/strategy',
//             type: 'GET'
//         });


//         $("#strategyLabel").text(toCamelCase(response) === 'One_three_two_six' ? 'Stochastic' : toCamelCase(response));


//         getFreezeStateAsync();


//     } catch (error) {
//         console.log('Error:', error);
//         alert('Operation failed: ' + error.responseText);
//     }
// }


// function turnOnOffSkip() {
//     const button = document.getElementById("skipButton");

//     if (freezeState === 'Y') {
//         freezeStat = 'N';
//         setShieldValue('OFF');
//     } else {

//         setShieldValue('ON');

//     }
// }


// Function to modify the date
function modifyDate(dateString, days, options) {
    const dateObj = new Date(dateString);
    dateObj.setUTCDate(dateObj.getUTCDate() + days);
    return dateObj.toLocaleDateString('en-CA', options);  // Return the new date in YYYY-MM-DD format
}

async function getJournal(purpose) {


    const currentDate = new Date();
    const options = {timeZone: 'UTC', year: 'numeric', month: '2-digit', day: '2-digit'};
    const utcDate = currentDate.toLocaleDateString('en-CA', options); // Format: YYYY-MM-DD

    // Set dateCreated initially if not already set
    dateCreated = dateCreated || utcDate;

    // Modify dateCreated based on purpose
    dateCreated = purpose === 'back'
        ? modifyDate(dateCreated, -1, options)
        : purpose === 'forward'
            ? modifyDate(dateCreated, 1, options)
            : utcDate;

    try {
        const response = await $.ajax({
            url: '/api/journals/dateCreated',
            type: 'GET',
            data: {dateCreated}
        });


        $('#journalContainer').empty();

        $('#dateTime').text(dateCreated);


        totalProfit = 0;

        const validJournals = response.filter(journal => journal.hand !== 0).reverse();
        playedShoe = 0;

        validJournals.forEach((journal, index) => {
            const journalDiv = document.createElement('div');
            journalDiv.classList.add('bg-white', 'border', 'border-gray-300', 'rounded-lg', 'p-2', 'mb-2');

            // Create a flex container for Shoe, Hand, and Profit
            const flexContainer = document.createElement('div');
            flexContainer.classList.add('flex', 'justify-between', 'items-center', 'text-xs', 'text-gray-700');

            flexContainer.id = `${journal.journalId}`;
            flexContainer.style.cursor = 'pointer';
            // Add the click event listener to the div
            flexContainer.addEventListener('click', function () {

                getGameArchive(flexContainer.id)
            });


            // Create container for left-aligned content (Win/Lose)
            const leftContainer = document.createElement('div');
            leftContainer.classList.add('flex-grow', 'text-left');

            // Create "Win/Lose" label
            const winLoseLabel = document.createElement('h4');
            winLoseLabel.classList.add('text-xs', 'ml-2', 'block');
            winLoseLabel.textContent = 'Win/Lose';

            // Create "20/20" value on a new line
            const winLoseValue = document.createElement('span');
            winLoseValue.classList.add('text-xs', 'ml-2', 'block');
            winLoseValue.textContent = `${journal.winLose}`;  // Adjust the values as needed

            // Append Win/Lose label and value
            leftContainer.appendChild(winLoseLabel);
            leftContainer.appendChild(winLoseValue);

            // Create container for center-aligned content (Shoe)
            const centerContainer = document.createElement('div');
            centerContainer.classList.add('flex-none', 'text-center');
            const shoeHeading = document.createElement('h4');
            shoeHeading.classList.add('font-semibold', 'text-sm');

            if (platform === "Baccarat") {
                shoeHeading.textContent = `Shoe: ${journal.shoe}`;
            } else {
                shoeHeading.textContent = `Round: ${journal.shoe}`;
            }


            centerContainer.appendChild(shoeHeading);

            // can you aslo put 'statregy' label under the Shoe
            // Create Strategy label and append it under the Shoe heading
            const strategyLabel = document.createElement('span');
            strategyLabel.classList.add('font-normal', 'text-xs');
            strategyLabel.textContent = `${toCamelCase(journal.strategy.replace("_", " "))}`;
            centerContainer.appendChild(strategyLabel);


            // Create container for right-aligned content (Hand, Profit)
            const rightContainer = document.createElement('div');
            rightContainer.classList.add('flex-grow', 'text-right');


            if (platform === "Baccarat") {
                rightContainer.innerHTML = `
                        <p>Hands: ${journal.hand}</p>
                        <p>Profit: ${journal.profit}</p>`;
            } else {
                rightContainer.innerHTML = `
                        <p>Rolls: ${journal.hand}</p>
                        <p>Profit: ${journal.profit}</p>`;
            }


            totalProfit += journal.profit;

            // Append elements
            flexContainer.appendChild(leftContainer);
            flexContainer.appendChild(centerContainer);
            flexContainer.appendChild(rightContainer);
            journalDiv.appendChild(flexContainer);
            $('#journalContainer').append(journalDiv);


            playedShoe++;
        });


        $('#totalProfit').text(totalProfit);


        getProfitGraph("");

    } catch (xhr) {
        console.error('Error fetching journals:', xhr);
        //  alert('An error occurred while fetching journals. Please try again.');
    }
    await fetchGameParameters();
    // await fetchGameParameters();


}

async function currentState() {


    try {
        const statusMessage = $('#status').text();
        const response = await $.ajax({
            url: '/api/sicBo/current-state',
            type: 'GET',
            data: {message: statusMessage}
        });
        displayResultToUi(response);
        showorHideStrategiesDots();
    } catch (xhr) {
        console.error('Error fetching current state:', xhr);
        $('#status').text(`Error: ${xhr.responseText}`);
    }


}


async function getGameArchive(journalId) {
    try {
        const response = await $.ajax({
            url: '/api/journals/get-archive',
            type: 'GET',
            data: {journalId}
        });

        // Handle the successful response
        displayResultToUi(response);

        // const riskLevelImage = document.getElementById('riskLevelImage');
        // riskLevelImage.onclick = null; // Disable click event

        // Update UI with archive information
        $('#infoHeader').text("Archive");
        $('#infoContent').text("Note: This is for viewing purposes only and cannot be modified. Please refresh your page to restore the original state.");
    } catch (error) {
        // Log the error details in case of failure
        console.error('Error fetching archive:', error);
        // Optionally, display an error message in the UI
        $('#infoHeader').text("Error");
        $('#infoContent').text("Failed to fetch the game archive. Please try again later.");
    }
}

function turnOnOffSkip() {
    const button = document.getElementById("skipButton");

    if (button.classList.contains("from-yellow-600")) {

        setShieldValue('OFF');
    } else {
        setShieldValue('ON');

    }
}


function getHandResult(response) {

    let handResult = response.handResult;
    let skipState = response.skipState;


    const handResultContainer = document.getElementById('handResultContainer');
    handResultContainer.innerHTML = "";

    if (!handResult) {
        return;
    }


    const fragment = document.createDocumentFragment();


    handResult.replace(/[^WL*]/g, '').split('').forEach((result, index) => {
        const circle = document.createElement('div');
        const isWin = result === 'W';
        const isLoss = result === 'L';
        const isStar = result === '*';

        // console.log(result);

        const isShouldSkip = skipState[index] === 'Y';


        circle.className = `flex items-center justify-center w-5 h-5 rounded-full text-white text-xs ${isWin ? 'bg-green-500' : isLoss ? 'bg-red-500' : 'bg-yellow-500'}`;

        if (isStar) {
            circle.classList.add(isShouldSkip ? 'text-yellow-600' : 'text-white');
        } else {
            circle.classList.add(isShouldSkip ? 'text-yellow-500' : 'text-white');
        }

        if (isShouldSkip) {
            circle.classList.add('font-bold');  // Apply bold text if skipState is true
        }

        // Set the inner text
        circle.innerText = isWin ? 'W' : isLoss ? 'L' : isStar ? '*' : '-';

        fragment.appendChild(circle);
    });

    handResultContainer.appendChild(fragment);


    setTimeout(() => {
        handResultContainer.scrollLeft = handResultContainer.scrollWidth;
    }, 0);
}


function updateProgressBar(percentageInput) {
    const percentage = percentageInput * 100; // Convert to percentage
    const progressBar = document.getElementById('progressBar');

    // Set the width of the progress bar
    progressBar.style.width = `${percentage}%`;

    // Change class based on percentage to adjust color
    if (percentage < 30) {
        progressBar.className = 'progress-bar low';
    } else if (percentage < sensitivityGlobal) {
        progressBar.className = 'progress-bar medium';
    } else {
        progressBar.className = 'progress-bar high';
    }
}


async function getActiveUsers() {
    try {
        const response = await $.ajax({
            url: '/active-users',  // Ensure this endpoint exists in your backend
            type: 'GET'            // Method type
        });

        $('#activeUsersCount').text(response);

    } catch (error) {
        // Log the error details
        console.error('Error fetching active users:', error);
    }
}


function handleViewChange() {
    const dayRadio = document.getElementById('dayRadio');
    const weekRadio = document.getElementById('weekRadio');

    // Determine the filter based on which radio button is checked
    const filterBy = dayRadio.checked ? 'Day' : weekRadio.checked ? 'Week' : null;

    if (filterBy) {
        getProfitGraph(filterBy);
    }
}


function getProfitGraph() {
    const dayRadio = document.getElementById('dayRadio');
    const filterBy = dayRadio.checked ? 'Day' : 'Week'; // Use a ternary operator for clarity

    fetch(`/api/journals/profit-data?filterBy=${filterBy}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.json();
        })
        .then(profitData => {
            // Destroy the existing chart if it exists
            if (dummyGraph) {
                try {
                    dummyGraph.destroy();
                } catch (error) {
                    console.error('Error destroying chart:', error);
                }
            }

            // Create a new chart instance
            dummyGraph = new Chart(ctx, {
                type: 'line',
                data: {
                    labels: profitData.labels,
                    datasets: [{
                        label: profitData.data[profitData.data.length - 1],
                        data: profitData.data,
                        fill: false,
                        borderColor: 'rgba(75, 192, 192, 1)',
                        tension: 0.1
                    }]
                },
                options: {
                    responsive: true,
                    scales: {
                        x: {
                            ticks: {
                                autoSkip: true, // Automatically skip ticks
                                maxTicksLimit: 10, // Limit the number of ticks
                                maxRotation: 45, // Rotate labels if needed
                                minRotation: 0 // Minimum label rotation
                            }
                        },
                        y: {
                            beginAtZero: true
                        }
                    }
                }
            });
        })
        .catch(error => {
            console.error('Error fetching profit data:', error);
        });
}


function showTab(tab) {
    // Containers
    const profitAndReportSection = document.getElementById('profitAndReportSection');
    const journalContainer = document.getElementById('journalContainer');
    const graphContainer = document.getElementById('graphContainer');
    const challengeContainer = document.getElementById('challengeContainer');


    // Tabs
    const journalTab = document.getElementById('journalTab');
    const graphTab = document.getElementById('graphTab');
    const challengeTab = document.getElementById('challengeTab');

    // Reset visibility of all containers
    journalContainer.classList.add('hidden');
    graphContainer.classList.add('hidden');
    challengeContainer.classList.add('hidden');
    profitAndReportSection.classList.add('hidden');

    // Reset styles for all tabs
    journalTab.classList.remove('border-blue-500', 'text-gray-700');
    graphTab.classList.remove('border-blue-500', 'text-gray-700');
    challengeTab.classList.remove('border-blue-500', 'text-gray-700');
    journalTab.classList.add('text-gray-500');
    graphTab.classList.add('text-gray-500');
    challengeTab.classList.add('text-gray-500');

    // Show the selected tab and apply active styling
    if (tab === 'History') {
        journalContainer.classList.remove('hidden');
        profitAndReportSection.classList.remove('hidden');
        journalTab.classList.add('border-blue-500', 'text-gray-700');
    } else if (tab === 'graph') {
        graphContainer.classList.remove('hidden');
        graphTab.classList.add('border-blue-500', 'text-gray-700');
    } else if (tab === 'challenge') {
        challengeContainer.classList.remove('hidden');
        challengeTab.classList.add('border-blue-500', 'text-gray-700');
    }
}


async function setShieldValue(yesNoValue) {

    try {
        const response = await $.ajax({
            url: '/api/sicBo/freeze-state',
            type: 'POST',
            data: {onOff: yesNoValue}
        });

        getFreezeStateAsync();

    } catch (error) {
        console.log('Error:', error);
        alert('Operation failed: ' + error.responseText);  // Optionally show an error message
    }
}


// async function setStrategyValue(strategyValue) {


//     try {
//         const response = await $.ajax({
//             url: '/api/sicBo/strategy',
//             type: 'POST',
//             data: { strategy: strategyValue }
//         });

//         getStrategy();

//     } catch (error) {
//         console.log('Error:', error);
//         alert('Operation failed: ' + error.responseText);  // Optionally show an error message
//     }
// }

async function getFreezeStateAsync() {

    const button = document.getElementById("skipButton");
    try {
        const response = await $.ajax({
            url: '/api/sicBo/get-freeze-state',
            type: 'GET'
        });

        // freezeState =  response;
        // console.log(response);
        // console.log(freezeState);
        await getPlatform();

        if (response === 'OFF') {

            freezeState = 'N'

            var recommendedBet = $("#recommendedBet").text();
            var suggestedBetUnitValue = $("#suggestedBetUnit").text();
            $('#nextbetText').text(recommendedBet.includes('Wait') ? "0" : suggestedBetUnitValue);

            button.classList.remove("bg-gradient-to-l", "from-yellow-600", "to-yellow-700");
            button.classList.add("bg-gradient-to-l", "from-gray-600", "to-gray-700");

        } else {

            // Revert back to the original yellow color
            button.classList.remove("bg-gradient-to-l", "from-gray-600", "to-gray-700");
            button.classList.add("bg-gradient-to-l", "from-yellow-600", "to-yellow-700");
            freezeState = 'Y'
            // $('#nextbetText').text("0");
        }

        // console.log(freezeState);

    } catch (error) {
        console.log('Error:', error);
        alert('Operation failed: ' + error.responseText);  // Optionally show an error message
    }
}


function showorHideStrategiesDots() {

    // const handCountValue = $("#handCount").text();
    // if (isSicbo === true) {

    // var pauseAndPlayValue = localStorage.getItem('pauseAndPlay');

    // console.log(pauseAndPlayValue);

    $('#resetButton').show();
    $('#threeDotsButton').show();

    // if (pauseAndPlayValue === 'play') {
    //     $('#resetButton').show();
    //     $('#threeDotsButton').show();
    // } else {
    //
    //     $('#threeDotsButton').hide();
    //     $('#resetButton').hide();
    //
    // }

    // }

}


// function hideStrategyDialog() {


//     const menu = document.getElementById('dropdownMenu');
//     const button = document.getElementById('threeDotsButton');
//     if (!menu.contains(event.target) && event.target !== button) {
//         menu.classList.add('hidden');
//     }

// }


function getNumbersFromString(str) {
    return str.replace(/\D/g, '');  // \D matches any non-digit character
}

function toCamelCase(str) {
    // Convert to lowercase and capitalize only the first letter
    return str.toLowerCase().replace(/^[a-z]/, (match) => match.toUpperCase());
}
