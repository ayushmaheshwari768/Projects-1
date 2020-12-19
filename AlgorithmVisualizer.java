package visualizer;

import javafx.application.Application;
import javafx.scene.*;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.geometry.*;
import javafx.event.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import java.util.*;
import javafx.util.Duration;
import javafx.animation.SequentialTransition;
import javafx.animation.FillTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class AlgorithmVisualizer extends Application {
	public final double DEFAULT_WIDTH = 1000;
	public double width;
	public final double MAX_HEIGHT = 500;
	public final double DEFAULT_SPEED = 10;
	private Label algorithmLabel;
	private ChoiceBox<String> algorithms;
	private Slider speed;
	private Label speedLabel;
	private TextField speedValue;
	private	Label randomLabel;
	private TextField numRandom;
	private Button enterRandom;
	private TextField numbers;
	private String[] nums;
	private Label numbersLabel;
	private Button enterNumbers;
	private Button reset;
	private double currentSpeed = DEFAULT_SPEED;
	private ArrayList<Rectangle> rectangles;
	private int numRectangles;
	private int heapSize;
	private HBox rectanglesBox;
	private HBox algorithmBox;
	private HBox speedBox;
	private HBox randomBox;
	private HBox numbersBox;
	private HBox resetBox;
	private VBox layout;
	private SequentialTransition seq = new SequentialTransition();
	private Scene scene;
	
	public void start(Stage stage) {
		algorithmLabel = new Label("Algorithms");
		algorithms = new ChoiceBox<String>();
		algorithms.getItems().addAll("Insertion Sort", "Merge Sort", "Quick Sort", "Heap Sort");
		
		speedLabel = new Label("Sorts Per Second");
		speed = new Slider(0.2, 25, DEFAULT_SPEED);
		speedValue = new TextField(Double.toString(speed.getValue()));
		speedValue.setAlignment(Pos.CENTER);
		speedValue.setMaxWidth(50);
		speed.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
				speedValue.textProperty().setValue(String.valueOf(Math.round((double) newValue * 10) / 10.0));
				currentSpeed = newValue.doubleValue();
			}
		});
		
		
		randomLabel = new Label("Number of Random Numbers To Sort:");
		numRandom = new TextField();
		enterRandom = new Button("Sort");
		
		numbersLabel = new Label("Enter Comma-Separated List of Numbers (No Spaces):");
		numbers = new TextField();
		enterNumbers = new Button("Sort");
		
		reset = new Button("Reset");
				
		Insets boxInsets = new Insets(10, 10, 10, 10);
		
		rectanglesBox = new HBox();
		rectanglesBox.setPrefSize(DEFAULT_WIDTH, MAX_HEIGHT);
		rectanglesBox.setStyle("-fx-background-color: #FFFFFF;");
		rectanglesBox.setAlignment(Pos.BOTTOM_LEFT);
		
		algorithmBox = new HBox();
		algorithmBox.setPadding(boxInsets);
		algorithmBox.setSpacing(10);
		algorithmBox.getChildren().addAll(algorithmLabel, algorithms);
		algorithmBox.setAlignment(Pos.CENTER);
		
		speedBox = new HBox();
		speedBox.setPadding(boxInsets);
		speedBox.setSpacing(10);
		speedBox.getChildren().addAll(speedLabel, speed, speedValue);
		speedBox.setAlignment(Pos.CENTER);
		
		randomBox = new HBox();
		randomBox.setPadding(boxInsets);
		randomBox.setSpacing(10);
		randomBox.getChildren().addAll(randomLabel, numRandom, enterRandom);
		randomBox.setAlignment(Pos.CENTER);
		
		numbersBox = new HBox();
		numbersBox.setPadding(boxInsets);
		numbersBox.setSpacing(10);
		numbersBox.getChildren().addAll(numbersLabel, numbers, enterNumbers);
		numbersBox.setAlignment(Pos.CENTER);
		
		resetBox = new HBox();
		resetBox.setPadding(boxInsets);
		resetBox.setSpacing(10);
		resetBox.getChildren().add(reset);
		resetBox.setAlignment(Pos.CENTER);
		
		layout = new VBox();
		layout.getChildren().addAll(rectanglesBox, algorithmBox, speedBox, randomBox, numbersBox, resetBox);
		layout.setStyle("-fx-background-color: #81BEF7;");
		
		EventHandler<MouseEvent> randomSortClicked = new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				try {
					currentSpeed = Double.parseDouble(speedValue.getText());
					if (currentSpeed < 0) {
						numRandom.setText("Invalid Sorts Per Second");
						return;
					}
				}
				catch (NumberFormatException e) {
					numRandom.setText("Invalid Sorts Per Second");
					return;
				}
				clear();
				if (algorithms.getValue() == null) {
					numRandom.setText("Select Algorithm");
					return;
				}
				try {
					if (Integer.parseInt(numRandom.getText()) < 0) {
						numRandom.setText("Invalid Input");
						return;
					}
					numRectangles = Integer.parseInt(numRandom.getText());
					
				}
				catch (NumberFormatException e) {
					numRandom.setText("Invalid Input");
					return;
				}
				numRectangles = Integer.parseInt(numRandom.getText());
				setWidth();
				stage.setWidth(width);
				createRandomRectangles();
				for (Rectangle r: rectangles) {rectanglesBox.getChildren().add(r);}
				
				if (algorithms.getValue().equals("Insertion Sort")) {
					insertionSort();
					seq.play();	
				}
				else if (algorithms.getValue().equals("Merge Sort")) {
					mergeSort(rectangles, 0, rectangles.size() - 1);
					seq.play();
				}
				else if (algorithms.getValue().equals("Quick Sort")) {
					quickSort(0, rectangles.size() - 1);
					seq.play();
				}
				else if (algorithms.getValue().equals("Heap Sort")) {
					heapSort();
					seq.play();
				}
			}
		};
		EventHandler<MouseEvent> unrandomSortClicked = new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				try {
					currentSpeed = Double.parseDouble(speedValue.getText());
					if (currentSpeed < 0) {
						numRandom.setText("Invalid Sorts Per Second");
						return;
					}
				}
				catch (NumberFormatException e) {
					numRandom.setText("Invalid Sorts Per Second");
					return;
				}
				clear();
				
				if (algorithms.getValue() == null) {
					numbers.setText("Select Algorithm");
					return;
				}
				nums = numbers.getText().split(",");
				numRectangles = nums.length;
				setWidth();
				stage.setWidth(width);
				boolean valid = createRectangles();
				if (!valid) {return;}
				for (Rectangle r:rectangles) {rectanglesBox.getChildren().add(r);}

				if (algorithms.getValue().equals("Insertion Sort")) {
					insertionSort();
					seq.play();	
				}
				else if (algorithms.getValue().equals("Merge Sort")) {
					mergeSort(rectangles, 0, rectangles.size() - 1);
					seq.play();
				}
				else if (algorithms.getValue().equals("Quick Sort")) {
					quickSort(0, rectangles.size() - 1);
					seq.play();
				}
				else if (algorithms.getValue().equals("Heap Sort")) {
					heapSort();
					seq.play();
				}
			}
		};
		EventHandler<MouseEvent> resetClicked = new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				clear();
				numRandom.clear();
				numbers.clear();
			}
		};
		
		enterRandom.addEventHandler(MouseEvent.MOUSE_CLICKED, randomSortClicked);
		enterNumbers.addEventHandler(MouseEvent.MOUSE_CLICKED, unrandomSortClicked);
		reset.addEventHandler(MouseEvent.MOUSE_CLICKED, resetClicked);
		
		
		Scene scene = new Scene(layout);
		stage.setScene(scene);
		stage.setX(200);
		stage.setY(80);
		stage.setMinHeight(MAX_HEIGHT + 250);
		stage.setMaxHeight(MAX_HEIGHT + 250);
		stage.setMinWidth(DEFAULT_WIDTH);
		stage.setTitle("Sorting Algorithm Visualizer");
		stage.show();
	}
	
	private boolean createRectangles() {
		ArrayList<Double> heights = new ArrayList<Double>();
		for (String num: nums) {
			try {
				heights.add(Double.parseDouble(num));
				if (Double.parseDouble(num) < 0) {
					numbers.setText("Invalid Input");
					return false;
				}
			}
			catch(NumberFormatException e) {
				numbers.setText("Invalid Input");
				return false;
			}
		}
		
		numRectangles = heights.size();
		double max = heights.get(0);
		for (int i = 0; i < numRectangles; i++) {
			if (heights.get(i) > max) {max = heights.get(i);}
		}
		for (int i = 0; i < heights.size(); i++) {
			Rectangle r = new Rectangle(width/numRectangles, (MAX_HEIGHT/max) * heights.get(i));
			r.setFill(Color.BLACK);
			r.setX(i * (width/numRectangles));
			rectangles.add(r);
		}
		return true;
	}
	
	private void createRandomRectangles() {
		for (int i = 0; i < numRectangles; i++) {
			int random = getRandomNumber(0, (int) MAX_HEIGHT);
			Rectangle r = new Rectangle(width/numRectangles, random);
			r.setFill(Color.BLACK);
			r.setX(i * (width/numRectangles));
			rectangles.add(r);
		}
	}
	
	private void setWidth() {
		width = DEFAULT_WIDTH;
		while (width % numRectangles != 0) {
			width++;
		}
		rectanglesBox.setPrefWidth(width);
	}
	
	private void insertionSort() {
		for (int i = 1; i < numRectangles; i++) {
			Rectangle current = rectangles.get(i);
			int j = i - 1;
			while (j >= 0 && rectangles.get(j).getHeight() > current.getHeight()) {
				Rectangle temp = rectangles.get(j);
				
				TranslateTransition trans1 = new TranslateTransition(); //transitions swap two rectangles
				trans1.setNode(current);
				trans1.setByX(-1 * width/numRectangles);
				trans1.setDuration(Duration.seconds(1/currentSpeed/2));
				trans1.setAutoReverse(false);
				trans1.setCycleCount(1);
				FillTransition fill1 = new FillTransition(Duration.seconds(1/currentSpeed/2), current, (Color) current.getFill(), Color.AQUAMARINE);
				TranslateTransition trans2 = new TranslateTransition();
				trans2.setNode(rectangles.get(j));
				trans2.setByX(width/numRectangles);
				trans2.setDuration(Duration.seconds(1/currentSpeed/2));
				trans2.setAutoReverse(false);
				trans2.setCycleCount(1);
				FillTransition fill2 = new FillTransition(Duration.seconds(1/currentSpeed/2), rectangles.get(j), (Color) rectangles.get(j).getFill(), Color.DARKRED);	
				fill2.setAutoReverse(true);
				fill2.setCycleCount(2);
				ParallelTransition par = new ParallelTransition(trans1, trans2, fill1, fill2);
				seq.getChildren().add(par);

				rectangles.get(j).setFill(Color.BLACK);
				rectangles.set(j, current);
				rectangles.set(j + 1, temp);
				j--;
			}
			seq.getChildren().add(new FillTransition(Duration.seconds(1/currentSpeed/2), current, (Color) current.getFill(), Color.BLACK));
		}
	}
	
	private void mergeSort(ArrayList<Rectangle> arr, int left, int right) {
		if (left < right) {
			int middle = (right + left) / 2;
			mergeSort(arr, left, middle);
			mergeSort(arr, middle + 1, right);
			merge(arr, left, middle, right);
		}
	}
	
	private void merge(ArrayList<Rectangle> arr, int left, int middle, int right) {
		int length1 = middle - left + 1;
        int length2 = right - middle;
        
		Rectangle[] leftArr = new Rectangle[length1];
		Rectangle[] rightArr = new Rectangle[length2];
		
		for (int i = 0; i < length1; i++) {
			leftArr[i] = arr.get(left + i);
		}   
        for (int j = 0; j < length2; j++) {
        	rightArr[j] = arr.get(middle + 1 + j);
        }
		
        int i = 0;
        int j = 0;
        int k = left;
        
        while (i < length1 && j < length2) {
        	if (leftArr[i].getHeight() <= rightArr[j].getHeight()) {
        		TranslateTransition trans = new TranslateTransition();
				trans.setNode(leftArr[i]);
				trans.setByX(k * (width/numRectangles) - leftArr[i].getX());
				leftArr[i].setX(leftArr[i].getX() + (k * (width/numRectangles) - leftArr[i].getX()));
				trans.setDuration(Duration.seconds(1/currentSpeed));
				trans.setAutoReverse(false);
				trans.setCycleCount(1);
				FillTransition fill = new FillTransition(Duration.seconds(1/currentSpeed/2), leftArr[i], (Color) leftArr[i].getFill(), Color.DARKRED);	
				fill.setAutoReverse(true);
				fill.setCycleCount(2);
				ParallelTransition p = new ParallelTransition(trans, fill);
				seq.getChildren().add(p);
				
        		arr.set(k, leftArr[i]);
                i++;
        	}
        	else {
        		TranslateTransition trans = new TranslateTransition();
				trans.setNode(rightArr[j]);
				trans.setByX(k * (width/numRectangles) - rightArr[j].getX());
				rightArr[j].setX(rightArr[j].getX() + (k * (width/numRectangles) - rightArr[j].getX()));
				trans.setDuration(Duration.seconds(1/currentSpeed));
				trans.setAutoReverse(false);
				trans.setCycleCount(1);
				FillTransition fill = new FillTransition(Duration.seconds(1/currentSpeed/2), rightArr[j], (Color) rightArr[j].getFill(), Color.DARKRED);	
				fill.setAutoReverse(true);
				fill.setCycleCount(2);
				ParallelTransition p = new ParallelTransition(trans, fill);
				seq.getChildren().add(p);
				
        		arr.set(k, rightArr[j]);
                j++;
        	}
        	k++;
        }
        
        while (i < length1) {
        	TranslateTransition trans = new TranslateTransition();
			trans.setNode(leftArr[i]);
			trans.setByX(k * (width/numRectangles) - leftArr[i].getX());
			leftArr[i].setX(leftArr[i].getX() + (k * (width/numRectangles) - leftArr[i].getX()));
			trans.setDuration(Duration.seconds(1/currentSpeed/2));
			trans.setAutoReverse(false);
			trans.setCycleCount(1);
			FillTransition fill = new FillTransition(Duration.seconds(1/currentSpeed/2), leftArr[i], (Color) leftArr[i].getFill(), Color.DARKRED);	
			fill.setAutoReverse(true);
			fill.setCycleCount(2);
			ParallelTransition p = new ParallelTransition(trans, fill);
			seq.getChildren().add(p);
			
        	arr.set(k, leftArr[i]);
            i++;
            k++;
        }
        while (j < length2) {
        	TranslateTransition trans = new TranslateTransition();
			trans.setNode(rightArr[j]);
			trans.setByX(k * (width/numRectangles) - rightArr[j].getX());
			rightArr[j].setX(rightArr[j].getX() + (k * (width/numRectangles) - rightArr[j].getX()));
			trans.setDuration(Duration.seconds(1/currentSpeed/2));
			trans.setAutoReverse(false);
			trans.setCycleCount(1);
			FillTransition fill = new FillTransition(Duration.seconds(1/currentSpeed/2), rightArr[j], (Color) rightArr[j].getFill(), Color.DARKRED);	
			fill.setAutoReverse(true);
			fill.setCycleCount(2);
			ParallelTransition p = new ParallelTransition(trans, fill);
			seq.getChildren().add(p);
			
        	arr.set(k, rightArr[j]);
            j++;
            k++;
        }
	}
	
	private void quickSort(int p, int r) {
		if (p < r) {
			int q = partition(p, r);
			quickSort(p, q - 1);
			quickSort(q + 1, r);
		}
	}
	
	private int partition(int p, int r) {
		double x = rectangles.get(r).getHeight();
		FillTransition pivotFill = new FillTransition(Duration.seconds(1/currentSpeed/2), rectangles.get(r), (Color) rectangles.get(r).getFill(), Color.AQUAMARINE);
		seq.getChildren().add(pivotFill);
		int i = p - 1;
		for(int j = p; j < r; j++) {
			if(rectangles.get(j).getHeight() <= x) {
				i++;
				swapRectangles(i, j, Color.DARKRED, Color.DARKRED);
			}
		}
		swapRectangles(i + 1, r, Color.DARKRED, Color.AQUAMARINE);
		FillTransition pivotUnfill = new FillTransition(Duration.seconds(1/currentSpeed/2), rectangles.get(r), (Color) rectangles.get(r).getFill(), Color.BLACK);
		seq.getChildren().add(pivotUnfill);
		return i + 1;
	}
	
	private void heapSort() {
		buildMaxHeap();
		for (int i = rectangles.size() - 1; i > 0; i--) {
			swapRectangles(0, i, Color.DARKRED, Color.DARKRED);
			heapSize--;
			maxHeapify(0);
		}
	}
	
	private void buildMaxHeap() {
		heapSize = rectangles.size();
		for (int i = rectangles.size() / 2; i >= 0; i--) {
			maxHeapify(i);
		}
	}
	
	private void maxHeapify(int index)
	   {
	      int left = left(index);
	      int right = right(index);
	      int largest = index;
	      if (left < heapSize && rectangles.get(left).getHeight() > rectangles.get(largest).getHeight())
	      {
	         largest = left;
	      }
	      if (right < heapSize && rectangles.get(right).getHeight() > rectangles.get(largest).getHeight())
	      {
	         largest = right;
	      }
	      if (largest != index)
	      {
	         swapRectangles(index, largest, Color.DARKRED, Color.DARKRED);
	         maxHeapify(largest);
	      }  
	   }
	
	private void swapRectangles(int r1, int r2, Color f1, Color f2) {
		TranslateTransition trans1 = new TranslateTransition();
		trans1.setNode(rectangles.get(r1));
		trans1.setByX(r2 * (width/numRectangles) - r1 * (width/numRectangles));
		rectangles.get(r1).setX(r2 * (width/numRectangles));
		trans1.setDuration(Duration.seconds(1/currentSpeed/2));
		trans1.setAutoReverse(false);
		trans1.setCycleCount(1);
		FillTransition fill1 = new FillTransition(Duration.seconds(1/currentSpeed/2), rectangles.get(r1), (Color) rectangles.get(r1).getFill(), f1);
		fill1.setAutoReverse(true);
		fill1.setCycleCount(2);
		TranslateTransition trans2 = new TranslateTransition();
		trans2.setNode(rectangles.get(r2));
		trans2.setByX(r1 * (width/numRectangles) - r2 * (width/numRectangles));;
		rectangles.get(r2).setX(r1 * (width/numRectangles));
		trans2.setDuration(Duration.seconds(1/currentSpeed/2));
		trans2.setAutoReverse(false);
		trans2.setCycleCount(1);
		FillTransition fill2 = new FillTransition(Duration.seconds(1/currentSpeed/2), rectangles.get(r2), (Color) rectangles.get(r2).getFill(), f2);	
		fill2.setAutoReverse(true);
		fill2.setCycleCount(2);
		ParallelTransition par = new ParallelTransition(trans1, trans2, fill1, fill2);
		seq.getChildren().add(par);
		
		Rectangle temp = rectangles.get(r1);
		rectangles.set(r1, rectangles.get(r2));
		rectangles.set(r2, temp);
	}
	
	private int getRandomNumber(int min, int max) {
	    return (int) ((Math.random() * (max - min)) + min);
	}
	
	private void clear() {
		rectangles = new ArrayList<Rectangle>();
		numRectangles = 0;
		rectanglesBox.getChildren().clear();
		seq.getChildren().clear();
	}
	
	private int left(int index) {
		return 2 * index + 1;
	}

	private int right(int index) {
		return 2 * index + 2;
	}
	
	public static void main(String[] args) {
		Application.launch(args);
	}
}
