package application;

    import javafx.scene.Scene;
    import javafx.scene.layout.VBox;
    import javafx.scene.layout.BorderPane;
    import javafx.stage.Stage;
    import javafx.scene.control.*;
    import javafx.geometry.Insets;
    import databasePart1.DatabaseHelper;

    public class QuestionAnswerPage {
        // reference to the same DB helper used at login
        private final DatabaseHelper databaseHelper;
        private final String username;

        public QuestionAnswerPage(DatabaseHelper databaseHelper, String username) {
            this.databaseHelper = databaseHelper;
            this.username = username;
        }

        public void show(Stage primaryStage) {
            Questions questions = databaseHelper.getAllQuestions();
            
            ListView<Question> listView = new ListView<>();
            listView.getItems().addAll(questions.getAllQuestions());
            
            // Render each question row
            listView.setCellFactory(param -> new ListCell<>() {
                @Override
                protected void updateItem(Question question, boolean empty) {
                    super.updateItem(question, empty);
                    if (empty || question == null) {
                        setText(null);
                    } else {
                        setText(question.getTitle() + (question.getResolved() ? " (resolved)" : " (unresolved)"));
                    }
                }
            });
            
            // Double-click to view details
            listView.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2) {
                    Question selected = listView.getSelectionModel().getSelectedItem();
                    if (selected != null) {
                        showQuestionDetail(primaryStage, selected.getId());
                    }
                }
            });
            
            Button backButton = new Button("Back");
            // If your StudentHomePage takes (DatabaseHelper) only, keep that:
         // === START EDITS ===
            backButton.setOnAction(a -> new StudentHomePage(databaseHelper, username).show(primaryStage));

            // === END EDITS ===


            Button askQuestionButton = new Button("Ask a Question");
            askQuestionButton.setOnAction(e -> showAskQuestionPage(primaryStage));
            
            VBox buttonBox = new VBox(10, backButton, askQuestionButton);
            buttonBox.setPadding(new Insets(10));

            BorderPane root = new BorderPane();
            root.setCenter(listView);
            root.setBottom(buttonBox);
            
            Scene scene = new Scene(root, 800, 400);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Questions");
        }
        
        // === START EDITS: detail page lists answers and propose/edit flows ===
        private void showQuestionDetail(Stage primaryStage, int questionId) {
            // always refresh from DB to show latest answers
            Question question = databaseHelper.getQuestion(questionId);

            Label title = new Label(question.getTitle());
            Label student = new Label("Asked by: " + question.getStudent());
            Label questionText = new Label(question.getQuestion());
            String str_status;
            if (question.getResolved() == false) {
            	str_status = "Unresolved";
            }
            else {
            	str_status = "Resolved";
            }
            Label status = new Label("Status: " + str_status);
            
            // ==Edits begin
            ListView<Question> clarificationsList = new ListView<>();
            clarificationsList.getItems().setAll(question.getClarifications().getAllQuestions());
            
            clarificationsList.setOnMouseClicked(e -> {
                Question selectedClar = clarificationsList.getSelectionModel().getSelectedItem();
                if (selectedClar != null && e.getClickCount() == 2) {  // double-click to open
                    showQuestionDetail(primaryStage, selectedClar.getId());
                }
            });
            //==Edits end

            // Answers list
            ListView<Answer> answersList = new ListView<>();
            answersList.getItems().setAll(question.getAnswers().getAll());

            // Buttons
            Button backButton = new Button("Back");
            backButton.setOnAction(e -> show(primaryStage));

            Button proposeBtn = new Button("Propose Answer");
            proposeBtn.setOnAction(e -> showAnswerForm(primaryStage, questionId, null));

            Button editBtn = new Button("Edit Selected");
            editBtn.setDisable(true);

            // Mark an answer as resolving and mark a question as resolved {
            
            Button markResolved = new Button("Mark as Resolved");
            markResolved.setDisable(true);
            
            answersList.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
                if (newV == null) {
                    markResolved.setDisable(true);
                } else {
                    if (username.equals(question.getStudent()) && question.getResolved() == false) {
                    	markResolved.setDisable(false);
                    }
                }
            });

            markResolved.setOnAction(e -> {
                Answer selected = answersList.getSelectionModel().getSelectedItem();
                if (selected != null && username.equals(question.getStudent()) && question.getResolved() == false) {
                	selected.setResolves(true);
                    question.setResolved(true);
                	databaseHelper.answerResolves(selected.getId(), selected.getResolves());
                    databaseHelper.questionResolved(question.getId(), question.getResolved());
                    markResolved.setDisable(true);
                    showQuestionDetail(primaryStage, question.getId());
                }
            });
            
            // }
            
            //==Edits start
            Button clarificationBtn = new Button("Ask for clarification");
            clarificationBtn.setOnAction(e -> clarificationPage(primaryStage, questionId));
            
            clarificationBtn.disableProperty().bind(
                clarificationsList.getSelectionModel().selectedItemProperty().isNotNull()
                    .or(answersList.getSelectionModel().selectedItemProperty().isNotNull())
            );
            // ==Edits end

            // Enable edit only if the selected answer belongs to this user
            answersList.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
                if (newV == null) {
                    editBtn.setDisable(true);
                } else {
                    editBtn.setDisable(!username.equals(newV.getName()));
                }
            });

            editBtn.setOnAction(e -> {
                Answer selected = answersList.getSelectionModel().getSelectedItem();
                if (selected != null && username.equals(selected.getName())) {
                    showAnswerForm(primaryStage, questionId, selected);
                }
            });

            VBox layout = new VBox(10, title, student, questionText, status, new Label("Clarifications: "), clarificationsList, new Label("Answers:"), answersList, proposeBtn, editBtn, clarificationBtn, backButton);
            layout.setPadding(new Insets(20));

            Scene detailScene = new Scene(layout, 800, 500);
            primaryStage.setScene(detailScene);
            primaryStage.setTitle("Question");
        }
        // === END EDITS ===
        
        // Page to pose a new question
        public void showAskQuestionPage(Stage primaryStage) {
            Label titleLabel = new Label("Title:");
            TextField titleField = new TextField();
            
            Label questionLabel = new Label("Question:");
            TextArea questionArea = new TextArea();
            questionArea.setPrefRowCount(5);
            
            Button submitButton = new Button("Submit");
            submitButton.setOnAction(e -> {
                String title = titleField.getText().trim();
                String questionText = questionArea.getText().trim();
                
                if (title.isEmpty() || questionText.isEmpty()) {
                    new Alert(Alert.AlertType.WARNING, "Please enter a title and question").showAndWait();
                } else {
                    databaseHelper.setQuestion(username, title, questionText, null);
                    show(primaryStage);
                }
            });
            
            Button cancelButton = new Button("Cancel");
            cancelButton.setOnAction(e -> show(primaryStage));
            
            VBox layout = new VBox(10, titleLabel, titleField, questionLabel, questionArea, submitButton, cancelButton);
            layout.setStyle("-fx-padding: 20;");
            
            Scene askScene = new Scene(layout, 800, 400);
            primaryStage.setScene(askScene);
            primaryStage.setTitle("Ask a Question");
        }

        // === START EDITS: unified form for new answer or to edit answer ===
        private void showAnswerForm(Stage primaryStage, int questionId, Answer toEdit) {
            boolean editing = (toEdit != null);

            Label prompt = new Label(editing ? "Update your answer:" : "Propose a new answer:");
            TextArea answerArea = new TextArea();
            answerArea.setPrefRowCount(5);
            if (editing) {
                answerArea.setText(toEdit.getAnswer());
            }

            Button submit = new Button(editing ? "Update" : "Submit");
            submit.setOnAction(e -> {
                String text = answerArea.getText().trim();
                if (text.isEmpty()) {
                    new Alert(Alert.AlertType.WARNING, "Please enter an answer.").showAndWait();
                    return;
                }
                if (editing) {
                    databaseHelper.updateAnswerText(toEdit.getANumber(), text);
                } else {
                    databaseHelper.setAnswer(username, text, questionId);
                }
                showQuestionDetail(primaryStage, questionId); // return to detail with refreshed data
            });

            Button cancel = new Button("Cancel");
            cancel.setOnAction(e -> showQuestionDetail(primaryStage, questionId));

            VBox layout = new VBox(10, prompt, answerArea, submit, cancel);
            layout.setPadding(new Insets(20));

            Scene scene = new Scene(layout, 700, 350);
            primaryStage.setScene(scene);
            primaryStage.setTitle(editing ? "Edit Answer" : "Propose Answer");
        }
        // === END EDITS ===
        
        //Edits begin
        private void clarificationPage(Stage primaryStage, int parentQuestionId) {           
            Label questionLabel = new Label("Question:");
            TextArea questionArea = new TextArea();
            questionArea.setPrefRowCount(5);
            
            Button submitButton = new Button("Submit");
            submitButton.setOnAction(e -> {
                String questionText = questionArea.getText().trim();
                
                if (questionText.isEmpty()) {
                    new Alert(Alert.AlertType.WARNING, "Please enter a question").showAndWait();
                } else {
                    databaseHelper.setQuestion(username, null, questionText, parentQuestionId);
                    show(primaryStage);
                }
            });
            
            Button cancelButton = new Button("Cancel");
            cancelButton.setOnAction(e -> show(primaryStage));
            
            VBox layout = new VBox(10, questionLabel, questionArea, submitButton, cancelButton);
            layout.setStyle("-fx-padding: 20;");
            
            Scene askScene = new Scene(layout, 800, 400);
            primaryStage.setScene(askScene);
            primaryStage.setTitle("Ask for Clarification");
        }
        //==Edits end
    }
