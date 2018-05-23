
// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
// exports.helloWorld = functions.https.onRequest((request, response) => {
//  response.send("Hello from Firebase!");
// });

const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

/*
exports.tokenUpdatedAt = functions.database.ref("USERS/{userId}/token")
.onWrite(event => {	
	const userRef = event.data.ref.parent;
	const tokenUpdatedAt = userRef.child('tokenUpdatedAt');
	const userId = event.params.userId;

	date = new Date();
	var formatDate =  date.getDate()+"-"+(date.getMonth()+1)+" "+date.getHours()+":"+date.getMinutes();
	return tokenUpdatedAt.set(formatDate).then(() => {
		return console.log('Token just updated by user: ', userId );	
	});
});
*/

exports.commentsWatcher = functions.database.ref("/DIALOGS_MESSAGES/{messageId}/{commentId}")
.onCreate((event, context) => {
	const commentObj = event.val();
	const root = event.ref.root;	
	const messageId = context.params.messageId;
	const userAvaRef = event.ref.child('userAvatar');
	const messageRef = root.child(`/DIALOGS/${messageId}`); 


	return messageRef.once('value').then( dialogSnap => {
		var messageObj = dialogSnap.val();
		// update last Comment
		dialogSnap.ref.child('lastComment').set(commentObj.userName + ": " + commentObj.message);
		// update updaetedAt
		dialogSnap.ref.child('updatedAt').set(commentObj.date);
		const usersRef = root.child(`USERS`);
		return usersRef.once('value').then(snapshot => {	
			var tokens = [];
			
			snapshot.forEach(child1 => {	
				childObj = child1.val();	
			
				if(childObj.id !== commentObj.userId && childObj.isReceiveNotifications )
				{
					if (childObj.isAdmin){
						console.log("Update notifications for ", childObj.fullName);						
						root.child(`/USERS_NOTIFICATIONS`).child(`/${childObj.id}/${messageId}/key`).set(`${messageId}`);
						if(childObj.token ){
							console.info("Token Added for User: " + childObj.fullName +", Token: ", childObj.token);
							tokens.push(childObj.token);		
						}			
					} else {
						dialogSnap.child('Members').forEach(member =>{
							if(childObj.id === member.key){				
								// update Notifications
								console.log("Update notifications for ", childObj.fullName);						
								root.child(`/USERS_NOTIFICATIONS`).child(`/${childObj.id}/${messageId}/key`).set(`${messageId}`);
								if(childObj.token ){
									console.info("Token Added for User: " + childObj.fullName +", Token: ", childObj.token);
									tokens.push(childObj.token);		
								}							
							} 	

						})
					}
				}	
			});
		
			// send Push
			var payload = {
				data:{						
					title: messageObj.title1,
					message: commentObj.userName + ": " + commentObj.message,			
					messageKey: messageId,
					senderId: commentObj.userId,
					senderName: commentObj.userName,
					receiverName: childObj.fullName,
					type: "COMMENT"
				}
			};
			return sendNessagesViaFCM(tokens, payload);			
		});
	});
});

exports.expenseWatcher = functions.database.ref("/EXPENSES/{year}/{month}/{reportId}")
.onWrite((event, context) => {
    var expenseObj = event.after.val();
    const month = context.params.month;
    const year = context.params.year;
	var aTitle = "Витрату оновлено";
	var aMessage = ": ";	

	if(!event.after.exists() && event.before.exists()){
		console.log("Expense removed ");
		expenseObj = event.before.val();
		aTitle = "Витрату видалено";
		aMessage = ": ";
	} else
	if(event.after.exists() && !event.before.exists()){
		console.log("Expense created ");		
		aTitle = "Витрату створено";
		aMessage = ": ";
	} else{
		console.log("Expense updated");		
    }
    const date = expenseObj.expenseDate.date+"."+ month+"."+year;
	var payload = {
		data:{						
			title: aTitle,
			message: expenseObj.userName + aMessage + expenseObj.title1+" ("+ expenseObj.price+" грн)",				
			expenseUserName: expenseObj.userName,
			expenseUserId: expenseObj.userId,
			expenseDate: date,
			type: "EXPENSE"
		}
	};
	return sendMessagesToAdminsExceptMe(payload, expenseObj.userId);		
});

exports.lessonWatcher = functions.database.ref("/LESSONS/{year}/{month}/{day}/{reportId}")
.onWrite((event, context) => {
    var lessonObj = event.after.val();
    const month = context.params.month;
	const year = context.params.year;
	const day = context.params.day;
	var aTitle = "Заняття оновлено";	

	if(!event.after.exists() && event.before.exists()){
		console.log("Lesson removed ");
		lessonObj = event.before.val();
		aTitle = "Заняття видалено";		
	} else
	if(event.after.exists() && !event.before.exists()){
		console.log("Lesson created ");		
		aTitle = "Заняття створено";		
	} else{
		console.log("Lesson updated");		
	}

	var minutes=lessonObj.dateTime.minutes;
	if(lessonObj.dateTime.minutes<10)
		minutes = "0" + minutes;
	if(lessonObj.dateTime.minutes===0)
		minutes = "00";
		
    const date = day+"."+ month+"."+year;
	var payload = {
		data:{						
			title: aTitle,
			message: lessonObj.userName + " " + date + " " + lessonObj.dateTime.hours + ":" + minutes,
			lessonUserName: lessonObj.userName,
			lessonUserId: lessonObj.userId,
			lessonKey: lessonObj.key,			
			lessonDate: date,
			type: "LESSON"
		}
	};
	return sendMessagesToAdminsExceptMe(payload, lessonObj.userId);		
});


exports.contactWatcher = functions.database.ref("/CONTACTS/{contactId}")
.onWrite((event, context) => {
    var contactObj = event.after.val();
	var aTitle = "Контакт оновлено";	

	if(!event.after.exists() && event.before.exists()){
		console.log("Contact removed ");
		contactObj = event.before.val();
		aTitle = "Контакт видалено";		
	} else
	if(event.after.exists() && !event.before.exists()){
		console.log("Contact created ");		
		aTitle = "Контакт створено";		
	} else{
		console.log("Contact updated");		
	}
			
	var aMessage = contactObj.name + "  " + contactObj.phone;
	if(contactObj.userName){
		aMessage = aMessage + " [" + contactObj.userName + "]";
	}
	var payload = {
		data:{						
			title: aTitle,
			message: aMessage,
			contactKey: contactObj.key,
			type: "CONTACT"
		}
	};
	return sendMessagesToAdmins(payload);		
});


function sendMessagesToAdmins(payload){
	return sendMessagesToAdminsExceptMe(payload, "");
}

function sendMessagesToAdminsExceptMe(payload, uId){
	var tokens = [];
	return admin.database().ref(`/USERS`).once('value').then(snapshot => {
		snapshot.forEach(user => {	
			userObj = user.val();
			if(userObj.isAdmin && userObj.token && userObj.id !== uId){	
				if(userObj.isReceiveNotifications){
					console.info("Token Added for: " + userObj.fullName +", userId: ", userObj.id);									
					tokens.push(userObj.token);
				} else {
					console.info("Token Skipped for: " + userObj.fullName +", userId: ", userObj.id);									
				}
			}			
		});//end for

		return sendNessagesViaFCM(tokens, payload);
	});
}


function sendMessagesToUsers(payload, uIds){
	var tokens = [];
	return admin.database().ref(`/USERS`).once('value').then(snapshot => {
		snapshot.forEach(user => {	
			userObj = user.val();
			if(userObj.token  && uIds.includes(userObj.id)){	
				if(userObj.isReceiveNotifications){
					console.info("Token Added for: " + userObj.fullName +", userId: ", userObj.id);									
					tokens.push(userObj.token);
				} else {
					console.info("Token Skipped for: " + userObj.fullName +", userId: ", userObj.id);									
				}
			}			
		});//end for

		return sendNessagesViaFCM(tokens, payload);
	});
}

function sendNessagesViaFCM(tokens, payload){
	if(tokens.length > 0)
		return  admin.messaging().sendToDevice(tokens, payload)
			.then(response => {
				console.log("Push Sent: ", response);
				return 0;
			})
			.catch(error => {
				console.log("Push Error: ", error);
			});			
	else 
		return console.log("Push No Tokens");
}

