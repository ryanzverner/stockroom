Sequel.migration do
  up do
    add_column :sows, :currency_code, String
  end

  down do
    drop_column :sows, :currency_code
  end
end
