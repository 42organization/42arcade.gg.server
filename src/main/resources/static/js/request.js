function js_request(form) {

    let url = form['url'].value;
    let params = form['params'].value;
    let method = form['method'].value;
    let token = form['token'].value;

    $.ajax({
        url: `${url}${params}`,
        type: `${method}`,
        contentType: 'application/json',
        headers: {
            "Authorization": "Bearer " + token
        },
        async: false,
        error: function () {
            alert("Error!");
        },
        success: function () {
            alert("success!");
        },
        complete: function () {
            history.replaceState({}, null, location.pathname);
        }
    })
}

function js_request_game_create(form) {

    let url = form['url'].value;
    let token = form['token'].value;
    let slotId = form['slotId'].value;
    let seasonId = form['seasonId'].value;
    let status = form['status'].value;

    let data = {
        "slotId": slotId,
        "seasonId": seasonId,
        "status": status
    };

    $.ajax({
        url: `${url}`,
        type: `POST`,
        headers: {
            "Authorization": "Bearer " + token
        },
        data: JSON.stringify(data),
        contentType: 'application/json',
        async: false,
        error: function () {
            alert("Error!");
        },
        success: function () {
            alert("success!");
        },
        complete: function () {
            history.replaceState({}, null, location.pathname);
        }
    })
}

function js_request_game_put(form) {

    let url = form['url'].value;
    let token = form['token'].value;
    let game_id = form['gameId'].value;
    let team1Score = form['team1Score'].value;
    let team2Score = form['team2Score'].value;
    let data = {
        "gameId": game_id,
        "team1Score": team1Score,
        "team2Score": team2Score
    };
    $.ajax({
        url: `${url}`,
        type: `PUT`,
        headers: {
            "Authorization": "Bearer " + token
        },
        data: JSON.stringify(data),
        contentType: 'application/json',
        async: false,
        error: function () {
            alert("Error!");
        },
        success: function () {
            alert("success!");
        },
        complete: function () {
            history.replaceState({}, null, location.pathname);
        }
    })
}


function js_request_season_post(form) {

    let url = form['url'].value;
    let token = form['token'].value;
    let season_name = form['season_name'].value;
    let start_time = form['start_time'].value;
    let start_ppp = form['start_ppp'].value;
    let ppp_gap = form['ppp_gap'].value;
    let data = {
        "seasonName": season_name,
        "startTime": start_time,
        "startPpp": start_ppp,
        "pppGap": ppp_gap
    };
    $.ajax({
        url: `${url}`,
        type: `POST`,
        headers: {
            "Authorization": "Bearer " + token
        },
        data: JSON.stringify(data),
        contentType: 'application/json',
        async: false,
        error: function () {
            alert("Error!");
        },
        success: function () {
            alert("success!");
        },
        complete: function () {
            history.replaceState({}, null, location.pathname);
        }
    })
}

function js_request_season_put(form) {

    let url = form['url'].value;
    let token = form['token'].value;
    let id = form['id'].value;
    let start_ppp = form['start_ppp'].value;
    let ppp_gap = form['ppp_gap'].value;
    let data = {
        "id": id,
        "startPpp": start_ppp,
        "pppGap": ppp_gap
    };
    $.ajax({
        url: `${url}`,
        type: `PUT`,
        headers: {
            "Authorization": "Bearer " + token
        },
        data: JSON.stringify(data),
        contentType: 'application/json',
        async: false,
        error: function () {
            alert("Error!");
        },
        success: function () {
            alert("success!");
        },
        complete: function () {
            history.replaceState({}, null, location.pathname);
        }
    })
}

function js_request_delete(form) {

    let token = form['token'].value;
    let id = "/" + form['id'].value;
    let url = form['url'].value + id;
    $.ajax({
        url: `${url}`,
        type: `DELETE`,
        headers: {
            "Authorization": "Bearer " + token
        },
        contentType: 'application/json',
        async: false,
        error: function () {
            alert("Error!");
        },
        success: function () {
            alert("success!");
        },
        complete: function () {
            history.replaceState({}, null, location.pathname);
        }
    })
}

function js_request_cron_put(form) {

    let url = form['url'].value;
    let token = form['token'].value;
    let second = form['second'].value;
    let interval_time = form['interval_time'].value;
    let start_time = form['start_time'].value;
    let end_time = form['end_time'].value;
    let data = {
        "cron": second + " */" + interval_time + " " + start_time + "-" + end_time + " * * *",
        "interval": interval_time
    };
    $.ajax({
        url: `${url}`,
        type: `PUT`,
        headers: {
            "Authorization": "Bearer " + token
        },
        data: JSON.stringify(data),
        contentType: 'application/json',
        async: false,
        error: function () {
            alert("Error!");
        },
        success: function () {
            alert("success!");
        },
        complete: function () {
            history.replaceState({}, null, location.pathname);
        }
    })
}

function js_request_rank_cron_put(form) {

    let url = form['url'].value;
    let token = form['token'].value;
    let second = form['second'].value;
    let minute = form['minute'].value;
    let hour = form['hour'].value;
    let data = {
        "cron": second + " " + minute + " " + hour + " * * *"
    };
    $.ajax({
        url: `${url}`,
        type: `PUT`,
        headers: {
            "Authorization": "Bearer " + token
        },
        data: JSON.stringify(data),
        contentType: 'application/json',
        async: false,
        error: function () {
            alert("Error!");
        },
        success: function () {
            alert("success!");
        },
        complete: function () {
            history.replaceState({}, null, location.pathname);
        }
    })
}

function js_request_rank_cron_run(form) {

    let url = form['url'].value;
    let token = form['token'].value;
    let data = {}
    $.ajax({
        url: `${url}`,
        type: `POST`,
        headers: {
            "Authorization": "Bearer " + token
        },
        contentType: 'application/json',
        data: JSON.stringify(data),
        async: false,
        error: function () {
            alert("Error!");
        },
        success: function () {
            alert("success!");
        },
        complete: function () {
            history.replaceState({}, null, location.pathname);
        }
    })
}

function js_request_path(form) {

    let url = form['url'].value;
    let path = form['path'].value;
    let method = form['method'].value;
    let token = form['token'].value;

    $.ajax({
        url: `${url}${path}`,
        type: `${method}`,
        contentType: 'application/json',
        headers: {
            "Authorization": "Bearer " + token
        },
        async: false,
        error: function () {
            alert("Error!");
        },
        success: function () {
            alert("success!");
        },
        complete: function () {
            history.replaceState({}, null, location.pathname);
        }
    })
}

function js_request_input(form) {
    let url = form['url'].value;
    let input_name = form['input'].name;
    let input_message = form['input'].value;
    let params = form['params'].value;
    let method = form['method'].value;
    let token = form['token'].value;

    /* 입력창 */
    let inputString = prompt(input_message, null);
    if (inputString == null)
        return;
    params = params + "&" + input_name + "=" + inputString;
    $.ajax({
        url: `${url}${params}`,
        type: `${method}`,
        contentType: 'application/json',
        headers: {
            "Authorization": "Bearer " + token
        },
        async: false,
        error: function () {
            alert("Error!");
        },
        success: function () {
            alert("success!");
        },
        complete: function () {
            history.replaceState({}, null, location.pathname);
        }
    })
}

function js_request_current_match_create(form) {

    let url = form['url'].value;
    let token = form['token'].value;
    let userId = form['userId'].value;
    let slotId = form['slotId'].value;
    let gameId = form['gameId'].value;
    let matchImminent = form['matchImminent'].value;
    let isMatched = form['isMatched'].value;

    let data = {
        'userId': userId,
        'slotId': slotId,
        'gameId': gameId,
        'matchImminent': matchImminent,
        'isMatched': isMatched
    };
    $.ajax({
        url: `${url}`,
        type: `POST`,
        headers: {
            "Authorization": "Bearer " + token
        },
        data: JSON.stringify(data),
        contentType: 'application/json',
        async: false,
        error: function () {
            alert("Error!");
        },
        success: function () {
            alert("success!");
        },
        complete: function () {
            history.replaceState({}, null, location.pathname);
        }
    })
}

function js_request_current_match_update(form) {

    let url = form['url'].value;
    let token = form['token'].value;
    let id = form['id'].value;
    let userId = form['userId'].value;
    let slotId = form['slotId'].value;
    let gameId = form['gameId'].value;
    let matchImminent = form['matchImminent'].value;
    let isMatched = form['isMatched'].value;

    let data = {
        'currentMatchId': id,
        'userId': userId,
        'slotId': slotId,
        'gameId': gameId,
        'matchImminent': matchImminent,
        'isMatched': isMatched
    };
    $.ajax({
        url: `${url}`,
        type: `PUT`,
        headers: {
            "Authorization": "Bearer " + token
        },
        data: JSON.stringify(data),
        contentType: 'application/json',
        async: false,
        error: function () {
            alert("Error!");
        },
        success: function () {
            alert("success!");
        },
        complete: function () {
            history.replaceState({}, null, location.pathname);
        }
    })
}

function js_request_slot_create(form) {

    let url = form['url'].value;
    let token = form['token'].value;
    let tableId = form['tableId'].value;
    let team1Id = form['team1Id'].value;
    let team2Id = form['team2Id'].value;
    let time = form['time'].value;
    let gamePpp = form['gamePpp'].value;
    let headCount = form['headCount'].value;
    let type = form['type'].value;

    let data = {
        'tableId': tableId,
        'team1Id': team1Id,
        'team2Id': team2Id,
        'time': time,
        'gamePpp': gamePpp,
        'headCount': headCount,
        'type': type
    };

    $.ajax({
        url: `${url}`,
        type: `POST`,
        headers: {
            "Authorization": "Bearer " + token
        },
        data: JSON.stringify(data),
        contentType: 'application/json',
        async: false,
        error: function () {
            alert("Error!");
        },
        success: function () {
            alert("success!");
        },
        complete: function () {
            history.replaceState({}, null, location.pathname);
        }
    })
}

function js_request_empty_slot_create(form) {

    let url = form['url'].value;
    let token = form['token'].value;
    let tableId = form['tableId'].value;
    let time = form['time'].value;

    let data = {
        'tableId': tableId,
        'team1Id': null,
        'team2Id': null,
        'time': time,
        'gamePpp': null,
        'headCount': null,
        'type': null
    };

    $.ajax({
        url: `${url}`,
        type: `POST`,
        headers: {
            "Authorization": "Bearer " + token
        },
        data: JSON.stringify(data),
        contentType: 'application/json',
        async: false,
        error: function () {
            alert("Error!");
        },
        success: function () {
            alert("success!");
        },
        complete: function () {
            history.replaceState({}, null, location.pathname);
        }
    })
}


function js_request_slot_update(form) {

    let url = form['url'].value;
    let token = form['token'].value;
    let id = form['id'].value;
    let gamePpp = form['gamePpp'].value;
    let headCount = form['headCount'].value;
    let type = form['type'].value;

    let data = {
        'slotId': id,
        'gamePpp': gamePpp,
        'headCount': headCount,
        'type': type
    };

    $.ajax({
        url: `${url}`,
        type: `PUT`,
        headers: {
            "Authorization": "Bearer " + token
        },
        data: JSON.stringify(data),
        contentType: 'application/json',
        async: false,
        error: function () {
            alert("Error!");
        },
        success: function () {
            alert("success!");
        },
        complete: function () {
            history.replaceState({}, null, location.pathname);
        }
    })
}

function js_request_slot_generator_put(form) {

    let url = form['url'].value;
    let token = form['token'].value;
    let startTime = form['startTime'].value;
    let interval = form['interval'].value;
    let slotNum = form['slotNum'].value;

    let data = {
        'startTime': startTime,
        'interval': interval,
        'slotNum': slotNum
    };

    $.ajax({
        url: `${url}`,
        type: `PUT`,
        headers: {
            "Authorization": "Bearer " + token
        },
        data: JSON.stringify(data),
        contentType: 'application/json',
        async: false,
        error: function () {
            alert("Error!");
        },
        success: function () {
            alert("success!");
        },
        complete: function () {
            history.replaceState({}, null, location.pathname);
        }
    })
}

function js_request_slot_generator_run(form) {

    let url = form['url'].value;
    let token = form['token'].value;

    $.ajax({
        url: `${url}`,
        type: `GET`,
        headers: {
            "Authorization": "Bearer " + token
        },
        contentType: 'application/json',
        async: false,
        error: function () {
            alert("Error!");
        },
        success: function () {
            alert("success!");
        },
        complete: function () {
            history.replaceState({}, null, location.pathname);
        }
    })
}

function js_request_noti_create(form) {

    let url = form['url'].value;
    let token = form['token'].value;
    let userId = form['userId'].value;
    let slotId = form['slotId'].value;
    let type = form['type'].value;
    let message = form['message'].value;
    let isChecked = form['isChecked'].value;

    let data = {
        'userId': userId,
        'slotId': slotId,
        'notiType': type,
        'message': message,
        'isChecked': isChecked,
        'sendMail': false
    };

    $.ajax({
        url: `${url}`,
        type: `POST`,
        headers: {
            "Authorization": "Bearer " + token
        },
        data: JSON.stringify(data),
        contentType: 'application/json',
        async: false,
        error: function () {
            alert("Error!");
        },
        success: function () {
            alert("success!");
        },
        complete: function () {
            history.replaceState({}, null, location.pathname);
        }
    })
}

function js_request_noti_create_with_mail(form) {

    let url = form['url'].value;
    let token = form['token'].value;
    let userId = form['userId'].value;
    let slotId = form['slotId'].value;
    let type = form['type'].value;
    let message = form['message'].value;
    let isChecked = form['isChecked'].value;

    let data = {
        'userId': userId,
        'slotId': slotId,
        'notiType': type,
        'message': message,
        'isChecked': isChecked,
        'sendMail': true
    };

    $.ajax({
        url: `${url}`,
        type: `POST`,
        headers: {
            "Authorization": "Bearer " + token
        },
        data: JSON.stringify(data),
        contentType: 'application/json',
        async: false,
        error: function () {
            alert("Error!");
        },
        success: function () {
            alert("success!");
        },
        complete: function () {
            history.replaceState({}, null, location.pathname);
        }
    })
}

function js_request_noti_create_all(form) {

    let url = form['url'].value;
    let token = form['token'].value;
    let message = form['message'].value;

    let data = {
        'message': message
    };

    $.ajax({
        url: `${url}`,
        type: `POST`,
        headers: {
            "Authorization": "Bearer " + token
        },
        data: JSON.stringify(data),
        contentType: 'application/json',
        async: false,
        error: function () {
            alert("Error!");
        },
        success: function () {
            alert("success!");
        },
        complete: function () {
            history.replaceState({}, null, location.pathname);
        }
    })
}

function js_request_noti_update(form) {

    let url = form['url'].value;
    let token = form['token'].value;
    let notiId = form['id'].value;
    let userId = form['userId'].value;
    let slotId = form['slotId'].value;
    let type = form['type'].value;
    let message = form['message'].value;
    let isChecked = form['isChecked'].value;

    let data = {
        'notiId': notiId,
        'userId': userId,
        'slotId': slotId,
        'notiType': type,
        'message': message,
        'isChecked': isChecked
    };

    $.ajax({
        url: `${url}`,
        type: `PUT`,
        headers: {
            "Authorization": "Bearer " + token
        },
        data: JSON.stringify(data),
        contentType: 'application/json',
        async: false,
        error: function () {
            alert("Error!");
        },
        success: function () {
            alert("success!");
        },
        complete: function () {
            history.replaceState({}, null, location.pathname);
        }
    })
}

function js_request_pchange_create(form) {

    let url = form['url'].value;
    let token = form['token'].value;
    let gameId = form['gameId'].value;
    let userId = form['userId'].value;
    let pppChange = form['pppChange'].value;
    let pppResult = form['pppResult'].value;

    let data = {
        'gameId': gameId,
        'userId': userId,
        'pppChange': pppChange,
        'pppResult': pppResult
    };

    $.ajax({
        url: `${url}`,
        type: `POST`,
        headers: {
            "Authorization": "Bearer " + token
        },
        data: JSON.stringify(data),
        contentType: 'application/json',
        async: false,
        error: function () {
            alert("Error!");
        },
        success: function () {
            alert("success!");
        },
        complete: function () {
            history.replaceState({}, null, location.pathname);
        }
    })
}

function js_request_pchange_update(form) {

    let url = form['url'].value;
    let token = form['token'].value;
    let id = form['id'].value;
    let gameId = form['gameId'].value;
    let userId = form['userId'].value;
    let pppChange = form['pppChange'].value;
    let pppResult = form['pppResult'].value;

    let data = {
        'pchangeId': id,
        'gameId': gameId,
        'userId': userId,
        'pppChange': pppChange,
        'pppResult': pppResult
    };

    $.ajax({
        url: `${url}`,
        type: `PUT`,
        headers: {
            "Authorization": "Bearer " + token
        },
        data: JSON.stringify(data),
        contentType: 'application/json',
        async: false,
        error: function () {
            alert("Error!");
        },
        success: function () {
            alert("success!");
        },
        complete: function () {
            history.replaceState({}, null, location.pathname);
        }
    })
}

function js_request_user_post(form) {

    let url = form['url'].value;
    let token = form['token'].value;
    let intra_id = form['intra_id'].value;
    let email = form['email'].value;
    let image_uri = form['image_uri'].value;
    let racket_type = form['racket_type'].value;
    let status_message = form['status_message'].value;
    let ppp = form['ppp'].value;
    let data = {
        "intraId" : intra_id,
        "email" : email,
        "userImageUri" : image_uri,
        "racketType" : racket_type,
        "statusMessage" : status_message,
        "ppp" : ppp
    };
    $.ajax({
        url: `${url}`,
        type: `POST`,
        headers: {
            "Authorization": "Bearer " + token
        },
        data: JSON.stringify(data),
        contentType: 'application/json',
        async: false,
        error: function () {
            alert("Error!");
        },
        success: function () {
            alert("success!");
        },
        complete: function () {
            history.replaceState({}, null, location.pathname);
        }
    })
}

function js_request_user_put(form) {
    let url = form['url'].value;
    let token = form['token'].value;
    let id = form['id'].value;
    let image_uri = form['image_uri'].value;
    let racket_type = form['racket_type'].value;
    let status_message = form['status_message'].value;
    let ppp = form['ppp'].value;
    let data = {
        "userId" : id,
        "userImageUri" : image_uri,
        "racketType" : racket_type,
        "statusMessage" : status_message,
        "ppp" : ppp
    };
    $.ajax({
        url: `${url}`,
        type: `PUT`,
        headers: {
            "Authorization": "Bearer " + token
        },
        data: JSON.stringify(data),
        contentType: 'application/json',
        async: false,
        error: function () {
            alert("Error!");
        },
        success: function () {
            alert("success!");
        },
        complete: function () {
            history.replaceState({}, null, location.pathname);
        }
    })
}

function js_request_team_post(form) {
    let url = form['url'].value;
    let token = form['token'].value;
    let team_ppp = form['teamPpp'].value;
    let team_headcount = form['headCount'].value;
    let team_score = form['score'].value;
    let data = {
        "teamPpp" : team_ppp,
        "headCount" : team_headcount,
        "score" : team_score,
    };
    $.ajax({
        url: `${url}`,
        type: `POST`,
        headers: {
            "Authorization": "Bearer " + token
        },
        data: JSON.stringify(data),
        contentType: 'application/json',
        async: false,
        error: function () {
            alert("Error!");
        },
        success: function () {
            alert("success!");
        },
        complete: function () {
            history.replaceState({}, null, location.pathname);
        }
    })
}

function js_request_team_put(form) {

    let url = form['url'].value;
    let token = form['token'].value;
    let id = form['id'].value;
    let team_user1 = form['teamUser1'].value;
    let team_user2 = form['teamUser2'].value;
    let team_ppp = form['teamPpp'].value;
    let team_headcount = form['teamHeadCount'].value;
    let team_score = form['teamScore'].value;
    let team_win = form['teamWin'].value;
    let data = {
        "teamId" : id,
        "user1Id" : team_user1,
        "user2Id" : team_user2,
        "teamPpp" : team_ppp,
        "headCount" : team_headcount,
        "score" : team_score,
        "win" : team_win
    };
    $.ajax({
        url: `${url}`,
        type: `PUT`,
        headers: {
            "Authorization": "Bearer " + token
        },
        data: JSON.stringify(data),
        contentType: 'application/json',
        async: false,
        error: function () {
            alert("Error!");
        },
        success: function () {
            alert("success!");
        },
        complete: function () {
            history.replaceState({}, null, location.pathname);
        }
    })
}

function js_request_team_delete(form) {

    let token = form['token'].value;
    let id = "/" + form['id'].value;
    let url = form['url'].value + id;
    $.ajax({
        url: `${url}`,
        type: `DELETE`,
        headers: {
            "Authorization": "Bearer " + token
        },
        contentType: 'application/json',
        async: false,
        error: function () {
            alert("Error!");
        },
        success: function () {
            alert("success!");
        },
        complete: function () {
            history.replaceState({}, null, location.pathname);
        }
    })
}

function js_request_feedback_put(form) {

    let url = form['url'].value;
    let token = form['token'].value;
    let feedback_id = form['feedbackId'].value;
    let is_solved = form['isSolved'].value;
    let data = {
        "feedbackId": feedback_id,
        "isSolved": is_solved
    };
    $.ajax({
        url: `${url}`,
        type: `PUT`,
        headers: {
            "Authorization": "Bearer " + token
        },
        data: JSON.stringify(data),
        contentType: 'application/json',
        async: false,
        error: function () {
            alert("Error!");
        },
        success: function () {
            alert("success!");
        },
        complete: function () {
            history.replaceState({}, null, location.pathname);
        }
    })
}