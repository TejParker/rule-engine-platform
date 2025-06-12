# https://happybase.readthedocs.org/en/latest/
# https://github.com/wbolster/happybase
import happybase

def main():
    HOST='172.27.170.34'
    PORT=9090
    # Will create and then delete this table
    TABLE_NAME='test_mock'
    ROW_KEY='row_key'
    
    print("Connecting to HBase at {0}:{1}".format(HOST, PORT))
    connection = happybase.Connection(HOST, PORT)

    tables = connection.tables()
    print("HBase has tables {0}".format(tables))

    if TABLE_NAME.encode() not in tables:
      print("Creating table {0}".format(TABLE_NAME))
      connection.create_table(TABLE_NAME, { 'family': dict() } )


    table = connection.table(TABLE_NAME)

    print("Storing values with row key '{0}'".format(ROW_KEY))
    table.put(ROW_KEY, {'family:qual1': 'value1',
                        'family:qual2': 'value2'})

    print("Getting values for row key '{0}'".format(ROW_KEY))
    row = table.row(ROW_KEY)
    
    # HBase返回的键是字节类型，需要使用字节字符串访问
    if row and b'family:qual1' in row:
        print("Value from family:qual1:", row[b'family:qual1'].decode('utf-8'))
    else:
        print("Warning: family:qual1 not found in row data")

    print("Printing rows with keys '{0}' and row-key-2".format(ROW_KEY))
    for key, data in table.rows([ROW_KEY, 'row-key-2']):
        print(key, data)

    print("Scanning rows with prefix 'row'")
    # row_prefix需要是字节类型
    for key, data in table.scan(row_prefix=b'row'):
        print(key, data)  # prints 'value1' and 'value2'

    print("Deleting row '{0}'".format(ROW_KEY))
    row = table.delete(ROW_KEY)

    print("Deleting table {0}".format(TABLE_NAME))
    connection.delete_table(TABLE_NAME, disable=True)

if __name__ == "__main__":
    main()