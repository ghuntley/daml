# DAML SDK documentation

This directory contains all of the documentation that gets published to docs.daml.com.

## Writing documentation

The docs are written in [reStructuredText](http://docutils.sourceforge.net/rst.html), built using [Sphinx](http://www.sphinx-doc.org/en/master/).

To edit documentation:

- Same as code: find the file, edit it on a branch, make a PR.
- For new files, best to copy existing ones, to get the formatting right.

  Don't forget you need to add your file to the `toctree` in `/docs/source/index.rst` *and* `/docs/configs/pdf/index.rst`.
- **Make sure you preview** before you push.

### Generated documentation

Not all of our docs are in rst files: some get generated. They are:

- the ledger API proto docs
- the DAML standard library reference
- the Java bindings reference

To edit those docs, edit the content inside the code source.

### Work-In-Progress Documentation

For documentation tasks that require significant time to create, you can use
the following approach to commit work-in-progress docs on `master`.

- place your sources files into a sub-directory `source-wip` directory and
  link them from the TOC in `wip/index.rst`
- add the line `wip/index` at the bottom of `source/index.rst`. 
- run the live preview script as explained below
- navigate to your docs from the TOC

Notes:
- Sadly, live preview only seems to detect changes in files linked from the
  master document, which is why the second step is required.
  Please make sure to not commit the change to `source/index.rst`. If you do
  nevertheless, CI will catch the problem, as the `source-wip` files are not
  available during a CI build.
- if you add a new sub-directory to the `source-wip` directory, you need to
  restart the live preview to have that sub-directory included in the
  documentation generation -- the symlinks need to be recreated.
- only sub-directories are supported in `source-wip`, not files. The HTML
  template we use seems to use the URL to determine whether to show an
  overview page or not.
- the whole process is a bit messy and generates some extra symlinks in the source directory; improvements welcome.


### Previewing

To preview the full docs, as deployed to docs.daml.com,run `scripts/preview.sh`.

To live-preview the docs, run `scripts/live-preview.sh`. The script accepts two flags:

- `--pdf` includes the PDF documentation
- `--gen` includes the generated documentation

Note that neither PDF, not generated docs will benefit from live updates. To update generated docs or PDF docs, quit the preview script with CTRL+C and start it again.

### Style conventions

For terminology and other style questions, follow the [main DA documentation style guide](https://docs.google.com/document/d/1dwE45gyxWXqlr4VTq9mJVnmSyBQ8V30ItucWBbCbViQ/edit).

A few pieces of RST guidance:

If you’re not familiar, it’s really worth reading the [primer](http://www.sphinx-doc.org/en/master/usage/restructuredtext/basics.html) for the basic syntax (emphasis, code text, lists, tables, images, comments, etc).
- Keep paragraphs all on the same line (no newlines/line breaks).
- Heading underlines in this hierarchical order:

  ```
  ######
  ******
  ======
  ------
  ^^^^^^
  """"""
  ```
- For internal links, use the `doc` directive where you can.
- For bullet points (unordered lists), use `-` (dashes).
- For code blocks, use the `literalinclude` directive if you can: it's best to source code from files that we test whether they compile.

## How the docs get built

The docs get built as part of the main `daml` repo CI, to make sure we don't break any links or do anything else that would cause Sphinx warnings.

## Publishing docs

### Prerequisites

To publish to the docs, you'll need to:

- Get access to the S3 bucket and Cloudfront (DA only).
- Create a directory to run everything from. Mine is `s3-env`.
- Install [AWS Google Auth tool](https://digitalasset.atlassian.net/wiki/spaces/IS/pages/719750045/AWS+Google+Auth) (DA only, internal link).
- Run the following:

```
# cd to the directory you created above
pipenv --python 3.6
pipenv install awscli
pipenv install aws-google-auth
```

### Once setup is done

After you've gone through the above steps, you can publish:

```
# if you're not already in it, cd to the directory you created above
pipenv shell
 # use google account password. Be fast!
aws-google-auth -u <yourname>@digitalasset.com
# cd to daml repo
# this bit takes a while. If it asks you about overwriting files, say yes
docs/scripts/publish.sh
 exit
```

 To publish to the staging website instead, use `docs/scripts/publish.sh --stage`.

## Testing code in docs

TBD
