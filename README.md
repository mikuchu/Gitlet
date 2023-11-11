# Gitlet Design Document

## Description
    implementing a version-control system that mimics
    some of the basic features of the popular system Git.


## Classes and Data Structures


### Commit
     Combinations of log messages, other metadata 
    (commit date, author, etc.), a reference to a tree, 
    and references to parent commits. The repository also maintains 
    a mapping from branch heads (in this course, we've used names 
    like master, proj2, etc.) to references to commits, 
    so that certain important commits have symbolic names.
#### Instance Variables
* Message - contains the message of a commit;
* Timestamp - time at whcich a commit was created. Assigned by the constructor
* Parent - the parent commit of a commit object

### SomeObj
    Class to store the Object, 
#### Instance Variables
* HashMap - to store commits
* HashMap - to store Branch
* directory - sets directory to directory
* workingdirectory = sets directory to workingdirectory



## Algorithms

###SomeObj Class
    
####init()
    Usage: java gitlet.Main init
    Description: new Gitlet version-control system in the current directory.
    Runtime: Constant
    Failure cases:  If there is already a Gitlet version-control system in the current directory
    Dangerous?: No

####add()
    Usage: java gitlet.Main add [file name]
    Description: Adds a copy of the file as it currently exists to the staging area 
    Runtime: lgN
    Failure cases: If the file does not exist,
    Dangerous?: No

####commit()
    Usage: java gitlet.Main commit [message]
    Description: Saves a snapshot of tracked files in the current commit and staging area so they can be restored at a later time, creating a new commit.
    Runtime: N
    Failure cases:  If no files have been staged, abort
    Dangerous?: No

####rm()
    Usage: java gitlet.Main rm [file name]
    Description: Unstage the file if it is currently staged for addition. 
    Runtime: Constant
    Failure cases:  If there is already a Gitlet version-control system in the current directory
    Dangerous?: No

####log()
    Usage:  java gitlet.Main log
    Description: Starting at the current head commit, display information about each commit backwards along the commit tree until the initial commit, following the first parent commit links, ignoring any second parents found in merge commits
    Runtime: N
    Failure cases:  None
    Dangerous?: No

####global_log()
    Usage: java gitlet.Main global-log
    Description: displays information about all commits ever made.
    Runtime: N
    Failure cases:  None
    Dangerous?: No

####find()
    Usage: java gitlet.Main find [commit message]
    Description: Prints out the ids of all commits that have the given commit message, one per line
    Runtime: Constant
    Failure cases:  If no such commit exists
    Dangerous?: No

####status()
    Usage: java gitlet.Main status
    Description: Displays what branches currently exist, and marks the current branch with a *. 
    Runtime: Constant
    Failure cases:  If there is already a Gitlet version-control system in the current directory
    Dangerous?: No

####checkout()
    Usage:  java gitlet.Main checkout -- [file name] 
            java gitlet.Main checkout [commit id] -- [file name]
            java gitlet.Main checkout [branch name]
    Description:    
            1.Takes the version of the file as it exists in the head commit, the front of the current branch, and puts it in the working directory, overwriting the version of the file that's already there if there is one.
            2.Takes the version of the file as it exists in the commit with the given id, and puts it in the working directory, overwriting the version of the file that's already there if there is one.
            3.Takes all files in the commit at the head of the given branch, and puts them in the working directory, overwriting the versions of the files that are already there if they exist. 

    Runtime: N
    Failure cases:  
            1.If the file does not exist in the previous commit, abort,
            2.If no commit with the given id exists,
            3.If no branch with that name exists

    Dangerous?: Yes

####branch()
    Usage: java gitlet.Main branch [branch name]
    Description: Creates a new branch with the given name, and points it at the current head node.
    Runtime: Constant
    Failure cases: If a branch with the given name already exists
    Dangerous?: No

####rm_branch()
    Usage: java gitlet.Main rm-branch [branch name]
    Description: Deletes the branch with the given name.
    Runtime: Constant
    Failure cases: If a branch with the given name does not exist, aborts.
    Dangerous?: No

####reset()
    Usage: java gitlet.Main reset [commit id]
    Description:  Checks out all the files tracked by the given commit.
    Runtime: N
    Failure cases:   If no commit with the given id exists
    Dangerous?: Yes

####merge()
    Usage: java gitlet.Main merge [branch name]
    Description: Merges files from the given branch into the current branch.
    Runtime: O(NlgN+D) , where N is the total number of ancestor commits for the two branches and D is the total amount of data in all the files under these commits.
    Failure cases:   If there are staged additions or removals present,If attempting to merge a branch with itself,
    Dangerous?: Yes

###Commit Class
####getMessage()
    return the contains the message of a commit;
####getTimestamp()
    return time at whcich a commit was created. Assigned by the constructor
####getParrent()
    return the parent commit of a commit object

## Persistence
*Use java.io.File and java.nio.file.Files to deal with files
* Since you are likely to keep various information in files (such as commits), you might be tempted to use apparently convenient file-system operations (such as listing a directory) to sequence through all of them. Be careful. Methods such as File.list and File.listFiles produce file names in an undefined order. If you use them to implement the log command, in particular, you can get random results.
* Windows users especially should beware that the file separator character is / on Unix (or MacOS) and '\' on Windows. So if you form file names in your program by concatenating some directory names and a file name together with explicit /s or \s, you can be sure that it won't work on one system or the other. Java provides a system-dependent file separator character File.separator, as in ".gitlet" + File.separator + "something", or the multi-argument constructors to File, as in \ new File(".gitlet", "something"), which you can use in place of ".gitlet/something").

