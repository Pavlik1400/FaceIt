"""
Module for testing authentification
"""


from face_recognitor import User


def main():
    """
    Main func
    """
    us = User(0)
    us.some_func_that_works_if_logged()
    us.exit()

if __name__ == '__main__':
    main()