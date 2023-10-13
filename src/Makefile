.PHONY: run
run: Main.class AFB.class AFBResult.class AFB_TSP.class AFB_TSP_Swap.class BirdMove.class Logger.class
	java -Xms6g -Xmx10g Main

.PHONY: clean
clean:
	rm ./*.class

.PHONY: test
test: Test.class AFB.class AFBResult.class AFB_TSP.class AFB_TSP_Swap.class BirdMove.class Logger.class
	java -ea -Xms6g -Xmx10g Test

Main.class: Main.java
	javac Main.java

AFB.class: AFB.java
	javac AFB.java

AFBResult.class: AFBResult.java
	javac AFBResult.java

AFB_TSP.class: AFB_TSP.java
	javac AFB_TSP.java

AFB_TSP_Swap.class: AFB_TSP_Swap.java
	javac AFB_TSP_Swap.java

BirdMove.class: BirdMove.java
	javac BirdMove.java

Test.class: Test.java
	javac Test.java

Logger.class: Logger.java
	javac Logger.java
