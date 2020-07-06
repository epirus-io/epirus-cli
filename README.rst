epirus-cli: Epirus Command Line Tools
==================================

.. image:: https://api.travis-ci.org/web3j/web3j-docs.svg?branch=master
   :target: https://docs.web3j.io
   :alt: Documentation Status

.. image:: https://travis-ci.org/epirus-io/epirus-cli.svg?branch=master
   :target: https://travis-ci.org/epirus-io/epirus-cli
   :alt: Build Status

.. image:: https://codecov.io/gh/epirus-io/epirus-cli/branch/master/graph/badge.svg
   :target: https://codecov.io/gh/epirus-io/epirus-cli
   :alt: codecov

.. image:: https://badges.gitter.im/web3j/web3j.svg
   :target: https://gitter.im/web3j/web3j
   :alt: Join the chat at https://gitter.im/web3j/web3j


About
=====
The Epirus command line tools enable developers to interact with blockchains more easily. The Epirus command line tools allow allow you to use some of the key functionality of Epirus using Web3j from your terminal, including:

* New project creation
* Project creation from existing Solidity code
* Wallet creation
* Wallet password management
* Ether transfer from one wallet to another
* Generation of Solidity smart contract wrappers
* Smart contract auditing


Installation
=====
On Linux/macOS, in a terminal, run the following command:

.. code-block:: bash

	curl -L get.epirus.io | sh

This script will not work if Epirus has been installed using Homebrew on macOS.

On Windows, in PowerShell, run the following command:

.. code-block:: bash

	Set-ExecutionPolicy Bypass -Scope Process -Force; iex ((New-Object System.Net.WebClient).DownloadString('https://raw.githubusercontent.com/epirus-io/epirus-installer/master/installer.ps1'))
   
Docs
=====

https://docs.epirus.io/sdk/cli/


Credits
=====

Smart contract auditing functionality is provided by `SmartCheck <https://github.com/smartdec/smartcheck>`_
