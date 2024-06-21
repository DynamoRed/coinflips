# Introduction

This file describes the various style and design conventions that are used in this repository.

# Git

Our Git style is inspired by the [style guidelines](http://karma-runner.github.io/6.4/dev/git-commit-msg.html) of the Git Karma implementation itself.

## Configuration
The configuration must be in the following format:

<details>
  <summary>.gitconfig</summary>

```
[user]
    name = <YOUR_NAME>
    email = <YOUR_EMAIL>
    username = <YOUR_USERNAME>
	signingkey = <YOUR_GPG_SIGNING_KEY>
[gpg]
	program = <YOUR_GPG_PROGRAM_PATH>
[core]
    autocrlf = input
    longpaths = true
	editor = code --wait
	pager = cat
	excludesfile = ~/.gitignore_global
	whitespace = -trailing-space
[commit]
    verbose = true
	gpgsign = true
[push]
    default = simple
    autoSetupRemote = true
[pull]
    rebase = true
[fetch]
	prune = true
[diff]
    ignoreSubmodules = dirty
	colorMoved = zebra
[color]
    ui = auto
    branch = auto
    diff = auto
    interactive = auto
    status = auto
[alias]
    br = branch
    i = init
    fl = flow
    c = commit
    cm = commit -m
    co = checkout
    ft = fetch
    rb = rebase
    rs = reset
    rv = revert
    st = status
    fta = fetch --all
    ps = push
    pl = pull
    pla = pull --all
    l = "log -16 --color=always --all --topo-order --pretty='%Cred%h%Creset -%C(yellow)%d%Creset %s %Cgreen(%cr) %C(bold blue)<%an>%Creset' --abbrev-commit --date=relative"
	lo = log --oneline
	sw = switch
	d = diff
    amend = commit --amend --no-edit
	untrack = rm --cache --
[filter "lfs"]
    clean = git-lfs clean -- %f
    smudge = git-lfs smudge -- %f
    process = git-lfs filter-process
    required = true
[help]
    autocorrect = 10
[tag]
  sort = version:refname
```
</details>

## Committing

1. The format of commit messages is as follows: `<type>[<references>]: <details>`
	1. `<type>` can be one of: `feat`, `hfix`, `bfix`, `init`, `lint` or `doc`.
	1. `<references>` is optional and must be in the format: `LFZ-<ID>` in which `<ID>` refers to the associated tracking ticket.
	1. Several `<references>` can be written, separated by `,`.
	1. The `<details>` must be in English, short but precise enough to understand the contents of the commit.
1. No environment files or files containing sensitive data (API keys, IP address, passwords, etc.) should be included in the commit.
1. The `git add .` or `git add *` command should be used with great caution.
1. *Optional:* Commits must be signed using a [GPG key](https://docs.gitlab.com/ee/user/project/repository/signed_commits/gpg.html)

## Branching

1. Branch names must be in kebab-case, in the format: `<reference>-<subject>`
*(Example: 1874-example-branch-name)*
	1. `<reference>` can be either the ID of a tracking ticket or the name of the branch type (hfix, develop, staging, production)
1. Protected branch names are as follows: `develop`,  `staging`, `production`

---

🪴 v**1.0.0**