package com.doctrack;

import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Scanner;
import java.util.Set;

public class Main {
    public static void main(String[] args) throws IOException, GitAPIException {
        Scanner in = new Scanner(System.in);
        System.out.println("Tracking folder: ");
        String folder = in.nextLine();
        Path gitFolderPath = Paths.get(folder, ".git");
        Git git = getGitForFolder(gitFolderPath);
        System.out.println("Done: " + git);
    }

    private static Git getGitForFolder(Path gitPath) throws IOException, GitAPIException {
        File gitPathFile = gitPath.toFile();
        boolean repoExists = gitPathFile.exists();
        Git git;
        try (Repository repo = new FileRepository(gitPath.toFile())) {
            if (!repoExists) {
                repo.create();
                git = new Git(repo);
                performInitialCommit(git);
            } else {
                git = new Git(repo);
            }
        }
        return git;
    }

    private static void performInitialCommit(Git git) throws IOException, GitAPIException {
        Path gitIgnoreFilePath = Paths.get(git.getRepository().getDirectory().getParent(),
                ".gitignore");
        File gitIgnoreFile = gitIgnoreFilePath.toFile();
        if (!gitIgnoreFile.exists()) {
            Files.writeString(gitIgnoreFilePath, ".DS_Store", StandardOpenOption.CREATE_NEW);
        }
        Status status = git.status().call();
        Set<String> untrackedFiles = status.getUntracked();
        AddCommand addCommand = git.add();
        untrackedFiles.forEach(addCommand::addFilepattern);
        addCommand.call();
        CommitCommand commitCommand = git.commit();
        commitCommand.setAuthor(Constants.GIT_USER, Constants.GIT_USER_EMAIL);
        commitCommand.setMessage(Constants.INITIAL_COMMIT_MSG);
        commitCommand.call();
    }
}
