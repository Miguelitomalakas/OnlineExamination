const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();

exports.notifyNewExam = functions.firestore
    .document("exams/{examId}")
    .onCreate(async (snap, context) => {
        const exam = snap.data();

        // Get all student users
        const studentUsers = await admin.firestore().collection("users")
            .where("role", "==", "STUDENT")
            .get();

        const tokens = [];
        studentUsers.forEach(userDoc => {
            const user = userDoc.data();
            if (user.fcmToken) {
                tokens.push(user.fcmToken);
            }
        });

        if (tokens.length > 0) {
            const payload = {
                notification: {
                    title: "New Exam Alert!",
                    body: `A new exam, '${exam.title}', has been created.`,
                },
            };

            try {
                await admin.messaging().sendToDevice(tokens, payload);
            } catch (error) {
                console.error("Error sending notification:", error);
            }
        }
    });
